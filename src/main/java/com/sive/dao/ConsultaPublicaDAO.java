package com.sive.dao;

import com.sive.model.ConsultaPublicaResponse.CriterioPontuacaoPublico;
import com.sive.model.ConsultaPublicaResponse.MovimentacaoPublica;
import com.sive.model.ConsultaPublicaResponse.ResponsavelPublico;
import com.sive.model.ConsultaPublicaResponse.SolicitacaoPublica;
import com.sive.model.ConsultaPublicaResponse.VizinhoFilaPublico;
import com.sive.model.StatusInscricao;
import com.sive.security.CpfCryptoService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO JDBC puro para consulta pública de posição na fila pelo responsável.
 */
public class ConsultaPublicaDAO {

    /**
     * Valida se o CPF do responsável e o protocolo pertencem a um mesmo vínculo
     * e retorna o responsável autenticado, ou null caso não correspondam.
     */
    public ResponsavelPublico autenticarPorCpfEProtocolo(String cpf, String protocolo) throws SQLException {
        String cpfHash = CpfCryptoService.gerarHash(cpf);
        String sql = "SELECT r.id, r.nome, r.cpf "
                + "FROM responsaveis r "
                + "INNER JOIN aluno_responsaveis ar ON ar.responsavel_id = r.id "
                + "INNER JOIN inscricoes i ON i.aluno_id = ar.aluno_id "
                + "WHERE r.cpf_hash = ? AND UPPER(i.protocolo) = UPPER(?) "
                + "LIMIT 1";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfHash);
            stmt.setString(2, protocolo.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ResponsavelPublico(
                            (UUID) rs.getObject("id"),
                            rs.getString("nome"),
                            CpfCryptoService.mascararParaExibicao(rs.getString("cpf")));
                }
            }
        }
        return null;
    }

    /**
     * Lista todas as solicitações (inscrições) dos filhos vinculados ao responsável.
     */
    public List<SolicitacaoPublica> listarSolicitacoesDoResponsavel(UUID responsavelId) throws SQLException {
        String sql = "SELECT "
                + "  i.id AS inscricao_id, "
                + "  i.protocolo, "
                + "  a.nome AS aluno_nome, "
                + "  a.data_nascimento, "
                + "  COALESCE(e.nome, 'Escola da rede') AS escola_nome, "
                + "  COALESCE(i.escola_id, t.escola_id) AS escola_ref_id, "
                + "  i.tipo_vaga, "
                + "  COALESCE(t.serie_ano, i.serie_ano) AS serie_ano, "
                + "  i.turno, "
                + "  i.status, "
                + "  COALESCE(i.pontuacao_total, 0) AS pontuacao_total, "
                + "  i.data_inscricao, "
                + "  i.data_chamada "
                + "FROM aluno_responsaveis ar "
                + "INNER JOIN alunos a ON a.id = ar.aluno_id "
                + "INNER JOIN inscricoes i ON i.aluno_id = a.id "
                + "LEFT JOIN turmas t ON t.id = i.turma_id "
                + "LEFT JOIN escolas e ON e.id = COALESCE(i.escola_id, t.escola_id) "
                + "WHERE ar.responsavel_id = ? "
                + "ORDER BY i.data_inscricao DESC";

        List<SolicitacaoPublica> lista = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, responsavelId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitacao(conn, rs));
                }
            }
        }
        return lista;
    }

    /**
     * Busca uma solicitação específica pelo número de protocolo para exibição direta via link.
     */
    public SolicitacaoPublica buscarSolicitacaoPorProtocolo(String protocolo) throws SQLException {
        String sql = "SELECT "
                + "  i.id AS inscricao_id, "
                + "  i.protocolo, "
                + "  a.nome AS aluno_nome, "
                + "  a.data_nascimento, "
                + "  COALESCE(e.nome, 'Escola da rede') AS escola_nome, "
                + "  COALESCE(i.escola_id, t.escola_id) AS escola_ref_id, "
                + "  i.tipo_vaga, "
                + "  COALESCE(t.serie_ano, i.serie_ano) AS serie_ano, "
                + "  i.turno, "
                + "  i.status, "
                + "  COALESCE(i.pontuacao_total, 0) AS pontuacao_total, "
                + "  i.data_inscricao, "
                + "  i.data_chamada "
                + "FROM inscricoes i "
                + "INNER JOIN alunos a ON a.id = i.aluno_id "
                + "LEFT JOIN turmas t ON t.id = i.turma_id "
                + "LEFT JOIN escolas e ON e.id = COALESCE(i.escola_id, t.escola_id) "
                + "WHERE UPPER(i.protocolo) = UPPER(?) "
                + "LIMIT 1";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, protocolo.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearSolicitacao(conn, rs);
                }
            }
        }
        return null;
    }

    private SolicitacaoPublica mapearSolicitacao(Connection conn, ResultSet rs) throws SQLException {
        SolicitacaoPublica item = new SolicitacaoPublica();
        UUID inscricaoId = (UUID) rs.getObject("inscricao_id");
        UUID escolaRefId = (UUID) rs.getObject("escola_ref_id");
        StatusInscricao status = StatusInscricao.fromString(rs.getString("status"));
        Date dataNascSql = rs.getDate("data_nascimento");
        Timestamp dataInscSql = rs.getTimestamp("data_inscricao");
        Timestamp dataChamadaSql = rs.getTimestamp("data_chamada");

        item.setInscricaoId(inscricaoId);
        item.setProtocolo(rs.getString("protocolo"));
        item.setAlunoNome(rs.getString("aluno_nome"));
        item.setIdadeAnos(calcularIdade(dataNascSql));
        item.setEscolaNome(rs.getString("escola_nome"));
        item.setTipoVaga(rs.getString("tipo_vaga"));
        item.setSerieAno(rs.getString("serie_ano"));
        item.setTurno(rs.getString("turno"));
        item.setStatus(status != null ? status : StatusInscricao.EM_ESPERA);
        item.setPontuacaoTotal(rs.getBigDecimal("pontuacao_total"));
        item.setDataInscricao(dataInscSql != null ? dataInscSql.toLocalDateTime() : null);
        item.setDataChamada(dataChamadaSql != null ? dataChamadaSql.toLocalDateTime() : null);

        preencherPosicaoEVizinhos(conn, item, escolaRefId);
        item.setCriterios(listarCriteriosDaInscricao(conn, inscricaoId));
        item.setHistorico(construirHistorico(item));
        return item;
    }

    private Integer calcularIdade(Date dataNascSql) {
        if (dataNascSql == null) {
            return null;
        }
        return Period.between(dataNascSql.toLocalDate(), LocalDate.now()).getYears();
    }

    private void preencherPosicaoEVizinhos(
            Connection conn,
            SolicitacaoPublica item,
            UUID escolaId) throws SQLException {
        if (item.getStatus() != StatusInscricao.EM_ESPERA || escolaId == null) {
            item.setPosicaoFila(null);
            item.setTotalNaFila(0);
            item.setPercentualAtendidas(item.getStatus() == StatusInscricao.CHAMADO
                    || item.getStatus() == StatusInscricao.MATRICULADO ? 100 : 0);
            return;
        }

        String sql = "WITH fila AS ( "
                + "  SELECT i.id, COALESCE(i.pontuacao_total, 0) AS pontuacao_total, "
                + "         ROW_NUMBER() OVER (ORDER BY COALESCE(i.pontuacao_total, 0) DESC, i.data_inscricao ASC) AS pos, "
                + "         COUNT(*) OVER () AS total "
                + "  FROM inscricoes i "
                + "  LEFT JOIN turmas t ON t.id = i.turma_id "
                + "  WHERE COALESCE(i.escola_id, t.escola_id) = ? "
                + "    AND i.status = 'EM_ESPERA' "
                + ") "
                + "SELECT pos, total FROM fila WHERE id = ?";

        int posicao = 0;
        int total = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, escolaId);
            stmt.setObject(2, item.getInscricaoId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    posicao = rs.getInt("pos");
                    total = rs.getInt("total");
                }
            }
        }

        item.setPosicaoFila(posicao > 0 ? posicao : null);
        item.setTotalNaFila(total);

        if (total > 0 && posicao > 0) {
            int atendidas = (int) Math.round(((double) (total - posicao) / total) * 100.0);
            item.setPercentualAtendidas(Math.max(0, Math.min(100, atendidas)));
        } else {
            item.setPercentualAtendidas(0);
        }

        List<VizinhoFilaPublico> vizinhos = new ArrayList<>();
        if (posicao > 1) {
            vizinhos.add(new VizinhoFilaPublico(
                    posicao - 1,
                    "#" + (posicao - 1) + "º — À frente",
                    "Maior pontuação nos critérios de prioridade",
                    false));
        }
        if (posicao > 0) {
            String pts = item.getPontuacaoTotal() != null
                    ? item.getPontuacaoTotal().setScale(1, RoundingMode.HALF_UP).toPlainString()
                    : "0.0";
            vizinhos.add(new VizinhoFilaPublico(
                    posicao,
                    "#" + posicao + "º — " + item.getAlunoNome(),
                    "Pontuação " + pts,
                    true));
        }
        if (posicao > 0 && posicao < total) {
            vizinhos.add(new VizinhoFilaPublico(
                    posicao + 1,
                    "#" + (posicao + 1) + "º — Atrás",
                    "Menor pontuação ou inscrição mais recente (desempate)",
                    false));
        }
        item.setVizinhos(vizinhos);
    }

    private List<CriterioPontuacaoPublico> listarCriteriosDaInscricao(
            Connection conn,
            UUID inscricaoId) throws SQLException {
        String sql = "SELECT c.nome, c.pontos_base, COALESCE(ic.pontos_obtidos, 0) AS pontos_obtidos "
                + "FROM criterios c "
                + "LEFT JOIN inscricao_criterios ic ON ic.criterio_id = c.id AND ic.inscricao_id = ? "
                + "WHERE c.ativo = TRUE "
                + "ORDER BY c.ordem ASC, c.nome ASC";

        List<CriterioPontuacaoPublico> criterios = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal pontosBase = rs.getBigDecimal("pontos_base");
                    BigDecimal pontosObtidos = rs.getBigDecimal("pontos_obtidos");
                    String detalhe = pontosObtidos != null && pontosObtidos.compareTo(BigDecimal.ZERO) > 0
                            ? "Critério pontuado"
                            : "Não aplicável";
                    criterios.add(new CriterioPontuacaoPublico(
                            rs.getString("nome"),
                            pontosBase != null ? pontosBase : BigDecimal.ZERO,
                            pontosObtidos != null ? pontosObtidos : BigDecimal.ZERO,
                            pontosBase != null ? pontosBase : BigDecimal.ZERO,
                            detalhe));
                }
            }
        }
        return criterios;
    }

    private List<MovimentacaoPublica> construirHistorico(SolicitacaoPublica item) {
        List<MovimentacaoPublica> eventos = new ArrayList<>();

        if (item.getStatus() == StatusInscricao.CHAMADO) {
            eventos.add(new MovimentacaoPublica(
                    "CHAMADO",
                    "Vaga disponível! Responsável chamado para confirmação.",
                    item.getDataChamada() != null ? item.getDataChamada() : LocalDateTime.now()));
        } else if (item.getStatus() == StatusInscricao.MATRICULADO) {
            eventos.add(new MovimentacaoPublica(
                    "MATRICULADO",
                    "Matrícula confirmada na unidade escolar.",
                    item.getDataChamada() != null ? item.getDataChamada() : LocalDateTime.now()));
        } else if (item.getStatus() == StatusInscricao.EM_ESPERA && item.getPosicaoFila() != null) {
            eventos.add(new MovimentacaoPublica(
                    "POSICAO",
                    "Posição atualizada: #" + item.getPosicaoFila(),
                    LocalDateTime.now()));
        }

        if (item.getDataInscricao() != null) {
            eventos.add(new MovimentacaoPublica(
                    "CONFIRMADA",
                    "Inscrição confirmada na lista de espera",
                    item.getDataInscricao()));
        }

        return eventos;
    }
}
