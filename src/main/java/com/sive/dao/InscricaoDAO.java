package com.sive.dao;

import com.sive.model.Aluno;
import com.sive.model.Inscricao;
import com.sive.model.InscricaoEscolaResumo;
import com.sive.model.StatusInscricao;
import com.sive.security.CpfCryptoService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InscricaoDAO {

    public List<InscricaoEscolaResumo> listarPorEscola(
            UUID escolaId,
            StatusInscricao status,
            String busca,
            String serieAno) throws SQLException {
        List<InscricaoEscolaResumo> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "WITH fila_ordenada AS ( "
                        + "  SELECT i.id, i.aluno_id, i.turma_id, i.tipo_vaga, i.serie_ano, i.turno, i.protocolo, "
                        + "         i.pontuacao_total, i.status, i.data_inscricao, i.data_chamada, "
                        + "         ROW_NUMBER() OVER (PARTITION BY i.status ORDER BY i.pontuacao_total DESC, i.data_inscricao ASC) AS posicao_calculada "
                        + "  FROM inscricoes i "
                        + "  LEFT JOIN turmas t ON t.id = i.turma_id "
                        + "  WHERE COALESCE(i.escola_id, t.escola_id) = ? "
                        + ") "
                        + "SELECT DISTINCT ON (f.id) "
                        + "  f.id, f.aluno_id, a.nome AS aluno_nome, a.data_nascimento AS aluno_data_nascimento, "
                        + "  f.turma_id, "
                        + "  COALESCE(t.serie_ano, f.serie_ano, CASE f.tipo_vaga WHEN 'CRECHE' THEN 'Creche' WHEN 'FUNDAMENTAL' THEN 'Fundamental' ELSE 'Lista de Espera' END) AS etapa_ou_turma, "
                        + "  f.tipo_vaga, f.turno, f.protocolo, f.pontuacao_total, f.status, "
                        + "  f.data_inscricao, f.data_chamada, f.posicao_calculada "
                        + "FROM fila_ordenada f "
                        + "INNER JOIN alunos a ON a.id = f.aluno_id "
                        + "LEFT JOIN turmas t ON t.id = f.turma_id "
                        + "LEFT JOIN aluno_responsaveis ar ON ar.aluno_id = a.id "
                        + "LEFT JOIN responsaveis r ON r.id = ar.responsavel_id "
                        + "WHERE f.status = ? ");

        boolean temSerieAno = serieAno != null && !serieAno.trim().isEmpty();
        if (temSerieAno) {
            sql.append("AND LOWER(COALESCE(t.serie_ano, f.serie_ano)) = LOWER(?) ");
        }

        boolean temBusca = busca != null && !busca.trim().isEmpty();
        String digitosBusca = temBusca ? CpfCryptoService.normalizarDigitos(busca) : "";
        boolean buscaPorCpfHash = temBusca && digitosBusca.length() == 11;

        if (buscaPorCpfHash) {
            sql.append("AND (a.cpf_hash = ? OR r.cpf_hash = ?) ");
        } else if (temBusca) {
            sql.append("AND (LOWER(a.nome) LIKE ? OR LOWER(r.nome) LIKE ?) ");
        }

        sql.append("ORDER BY f.id");

        String sqlFinal = "SELECT * FROM (" + sql + ") sub ORDER BY sub.pontuacao_total DESC, sub.data_inscricao ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlFinal)) {

            int idx = 1;
            stmt.setObject(idx++, escolaId);
            stmt.setString(idx++, status.name());

            if (temSerieAno) {
                stmt.setString(idx++, serieAno.trim());
            }

            if (buscaPorCpfHash) {
                String hash = CpfCryptoService.gerarHash(digitosBusca);
                stmt.setString(idx++, hash);
                stmt.setString(idx++, hash);
            } else if (temBusca) {
                String termoTexto = "%" + busca.trim().toLowerCase() + "%";
                stmt.setString(idx++, termoTexto);
                stmt.setString(idx++, termoTexto);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date dataNascSql = rs.getDate("aluno_data_nascimento");
                    Timestamp dataInscSql = rs.getTimestamp("data_inscricao");
                    Timestamp dataChamadaSql = rs.getTimestamp("data_chamada");
                    StatusInscricao itemStatus = StatusInscricao.valueOf(rs.getString("status"));
                    Integer posicao = itemStatus == StatusInscricao.MATRICULADO
                            ? null
                            : rs.getInt("posicao_calculada");

                    lista.add(new InscricaoEscolaResumo(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("aluno_id"),
                            rs.getString("aluno_nome"),
                            dataNascSql != null ? dataNascSql.toLocalDate() : null,
                            (UUID) rs.getObject("turma_id"),
                            rs.getString("etapa_ou_turma"),
                            rs.getString("tipo_vaga"),
                            rs.getString("turno"),
                            rs.getString("protocolo"),
                            rs.getBigDecimal("pontuacao_total"),
                            itemStatus,
                            dataInscSql != null ? dataInscSql.toLocalDateTime() : null,
                            dataChamadaSql != null ? dataChamadaSql.toLocalDateTime() : null,
                            posicao));
                }
            }
        }
        return lista;
    }

    public Map<StatusInscricao, Integer> contarPorStatus(UUID escolaId) throws SQLException {
        Map<StatusInscricao, Integer> contagens = new EnumMap<>(StatusInscricao.class);
        String sql = "SELECT i.status, COUNT(*) AS total "
                + "FROM inscricoes i "
                + "LEFT JOIN turmas t ON t.id = i.turma_id "
                + "WHERE COALESCE(i.escola_id, t.escola_id) = ? "
                + "GROUP BY i.status";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, escolaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StatusInscricao status = StatusInscricao.fromString(rs.getString("status"));
                    if (status != null) {
                        contagens.put(status, rs.getInt("total"));
                    }
                }
            }
        }
        return contagens;
    }

    public InscricaoEscolaResumo buscarPorIdEEscola(UUID inscricaoId, UUID escolaId) throws SQLException {
        String sql = "SELECT i.id, i.aluno_id, a.nome AS aluno_nome, a.data_nascimento AS aluno_data_nascimento, "
                + "i.turma_id, i.tipo_vaga, i.turno, i.protocolo, i.pontuacao_total, i.status, i.data_inscricao, i.data_chamada "
                + "FROM inscricoes i "
                + "INNER JOIN alunos a ON a.id = i.aluno_id "
                + "LEFT JOIN turmas t ON t.id = i.turma_id "
                + "WHERE i.id = ? AND COALESCE(i.escola_id, t.escola_id) = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);
            stmt.setObject(2, escolaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date dataNascSql = rs.getDate("aluno_data_nascimento");
                    Timestamp dataInscSql = rs.getTimestamp("data_inscricao");
                    Timestamp dataChamadaSql = rs.getTimestamp("data_chamada");
                    return new InscricaoEscolaResumo(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("aluno_id"),
                            rs.getString("aluno_nome"),
                            dataNascSql != null ? dataNascSql.toLocalDate() : null,
                            (UUID) rs.getObject("turma_id"),
                            null,
                            rs.getString("tipo_vaga"),
                            rs.getString("turno"),
                            rs.getString("protocolo"),
                            rs.getBigDecimal("pontuacao_total"),
                            StatusInscricao.valueOf(rs.getString("status")),
                            dataInscSql != null ? dataInscSql.toLocalDateTime() : null,
                            dataChamadaSql != null ? dataChamadaSql.toLocalDateTime() : null,
                            null);
                }
            }
        }
        return null;
    }

    public boolean chamar(UUID inscricaoId, UUID escolaId) throws SQLException {
        String sql = "UPDATE inscricoes SET status = 'CHAMADO', data_chamada = CURRENT_TIMESTAMP "
                + "WHERE id = ? AND COALESCE(escola_id, (SELECT t.escola_id FROM turmas t WHERE t.id = inscricoes.turma_id)) = ?";
        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);
            stmt.setObject(2, escolaId);
            return stmt.executeUpdate() > 0;
        }
    }

    public void chamar(UUID inscricaoId) throws SQLException {
        String sql = "UPDATE inscricoes SET status = 'CHAMADO', data_chamada = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);
            stmt.executeUpdate();
        }
    }

    public boolean atualizarStatusChamado(
            Connection conn,
            UUID inscricaoId,
            UUID escolaId,
            StatusInscricao novoStatus,
            UUID turmaId,
            boolean limparDataChamada) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE inscricoes SET status = ?");
        if (turmaId != null) {
            sql.append(", turma_id = ?");
        }
        if (limparDataChamada) {
            sql.append(", data_chamada = NULL");
        }
        sql.append(" WHERE id = ?");
        if (escolaId != null) {
            sql.append(" AND COALESCE(escola_id, (SELECT t.escola_id FROM turmas t WHERE t.id = inscricoes.turma_id)) = ?");
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setString(idx++, novoStatus.name());
            if (turmaId != null) {
                stmt.setObject(idx++, turmaId);
            }
            stmt.setObject(idx++, inscricaoId);
            if (escolaId != null) {
                stmt.setObject(idx++, escolaId);
            }
            return stmt.executeUpdate() > 0;
        }
    }

    public void atualizarStatusChamado(
            Connection conn,
            UUID inscricaoId,
            StatusInscricao novoStatus,
            UUID turmaId,
            boolean limparDataChamada) throws SQLException {
        atualizarStatusChamado(conn, inscricaoId, null, novoStatus, turmaId, limparDataChamada);
    }

    public boolean decrementarVagaTurma(Connection conn, UUID turmaId, UUID escolaId) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE turmas SET vagas_disponiveis = vagas_disponiveis - 1 "
                + "WHERE id = ? AND vagas_disponiveis > 0");
        if (escolaId != null) {
            sql.append(" AND escola_id = ?");
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setObject(1, turmaId);
            if (escolaId != null) {
                stmt.setObject(2, escolaId);
            }
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean decrementarVagaTurma(Connection conn, UUID turmaId) throws SQLException {
        return decrementarVagaTurma(conn, turmaId, null);
    }

    public List<Inscricao> listarInscricoesEmEsperaParaRecalculo(Connection conn) throws SQLException {
        String sql = "SELECT i.id AS inscricao_id, i.aluno_id, "
                + "a.renda_per_capita, a.tem_necessidade_especial, a.vulnerabilidade_socioeconomica, "
                + "a.responsavel_solo, a.mora_de_aluguel, a.bolsa_familia, a.bpc_loas, a.irmao_matriculado "
                + "FROM inscricoes i "
                + "INNER JOIN alunos a ON a.id = i.aluno_id "
                + "WHERE i.status = 'EM_ESPERA'";

        List<Inscricao> lista = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Aluno aluno = new Aluno();
                aluno.setId((UUID) rs.getObject("aluno_id"));
                aluno.setRendaPerCapita(rs.getBigDecimal("renda_per_capita"));
                aluno.setTemNecessidadeEspecial(rs.getBoolean("tem_necessidade_especial"));
                aluno.setVulnerabilidadeSocioeconomica(rs.getBoolean("vulnerabilidade_socioeconomica"));
                aluno.setResponsavelSolo(rs.getBoolean("responsavel_solo"));
                aluno.setMoraDeAluguel(rs.getBoolean("mora_de_aluguel"));
                aluno.setBolsaFamilia(rs.getBoolean("bolsa_familia"));
                aluno.setBpcLoas(rs.getBoolean("bpc_loas"));
                aluno.setIrmaoMatriculado(rs.getBoolean("irmao_matriculado"));

                Inscricao inscricao = new Inscricao();
                inscricao.setId((UUID) rs.getObject("inscricao_id"));
                inscricao.setAlunoId(aluno.getId());
                inscricao.setAluno(aluno);
                lista.add(inscricao);
            }
        }
        return lista;
    }

    public void atualizarPontuacaoTotal(
            Connection conn,
            UUID inscricaoId,
            BigDecimal pontuacaoTotal) throws SQLException {
        String sql = "UPDATE inscricoes SET pontuacao_total = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, pontuacaoTotal != null ? pontuacaoTotal : BigDecimal.ZERO);
            stmt.setObject(2, inscricaoId);
            stmt.executeUpdate();
        }
    }
}
