package com.sive.dao;

import com.sive.model.Aluno;
import com.sive.model.AlunoDetalhe;
import com.sive.model.AlunoResumo;
import com.sive.model.Endereco;
import com.sive.security.CpfCryptoService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlunoDAO {

    private static final String COLUNAS_ENDERECO = "cep, rua, numero, complemento, bairro, cidade, estado";
    private static final String COLUNAS_ENDERECO_ALIASED = "a.cep, a.rua, a.numero, a.complemento, a.bairro, a.cidade, a.estado";

    public Aluno inserir(Aluno aluno) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return inserir(aluno, conn);
        }
    }

    public Aluno inserir(Aluno aluno, Connection conn) throws SQLException {
        String sql = "INSERT INTO alunos (id, nome, cpf, cpf_hash, data_nascimento, "
                + COLUNAS_ENDERECO
                + ", renda_per_capita, tem_necessidade_especial, "
                + "vulnerabilidade_socioeconomica, responsavel_solo, mora_de_aluguel, "
                + "bolsa_familia, bpc_loas, irmao_matriculado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (aluno.getId() == null) {
            aluno.setId(UUID.randomUUID());
        }

        String cpfOriginal = aluno.getCpf();
        String cpfCriptografado = CpfCryptoService.criptografar(cpfOriginal);
        String cpfHash = CpfCryptoService.gerarHash(cpfOriginal);
        Endereco endereco = aluno.getEndereco();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, aluno.getId());
            stmt.setString(2, aluno.getNome());
            stmt.setString(3, cpfCriptografado);
            stmt.setString(4, cpfHash);
            stmt.setDate(5, aluno.getDataNascimento() != null ? Date.valueOf(aluno.getDataNascimento()) : null);
            stmt.setString(6, endereco != null ? endereco.getCep() : null);
            stmt.setString(7, endereco != null ? endereco.getRua() : null);
            stmt.setString(8, endereco != null ? endereco.getNumero() : null);
            stmt.setString(9, endereco != null ? endereco.getComplemento() : null);
            stmt.setString(10, endereco != null ? endereco.getBairro() : null);
            stmt.setString(11, endereco != null ? endereco.getCidade() : null);
            stmt.setString(12, endereco != null ? endereco.getEstado() : null);
            stmt.setBigDecimal(13, aluno.getRendaPerCapita() != null ? aluno.getRendaPerCapita() : BigDecimal.ZERO);
            stmt.setBoolean(14, aluno.isTemNecessidadeEspecial());
            stmt.setBoolean(15, aluno.isVulnerabilidadeSocioeconomica());
            stmt.setBoolean(16, aluno.isResponsavelSolo());
            stmt.setBoolean(17, aluno.isMoraDeAluguel());
            stmt.setBoolean(18, aluno.isBolsaFamilia());
            stmt.setBoolean(19, aluno.isBpcLoas());
            stmt.setBoolean(20, aluno.isIrmaoMatriculado());

            stmt.executeUpdate();
            aluno.setCpf(CpfCryptoService.mascararParaExibicao(cpfCriptografado));
            return aluno;
        }
    }

    public UUID inserirInscricaoFila(
            Connection conn,
            UUID escolaId,
            UUID alunoId,
            String tipoVaga,
            String serieAno,
            String turno,
            String protocolo,
            BigDecimal pontuacaoTotal) throws SQLException {
        UUID inscricaoId = UUID.randomUUID();
        String sql = "INSERT INTO inscricoes "
                + "(id, aluno_id, escola_id, turma_id, tipo_vaga, serie_ano, turno, protocolo, pontuacao_total, status) "
                + "VALUES (?, ?, ?, NULL, ?, ?, ?, ?, ?, 'EM_ESPERA')";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);
            stmt.setObject(2, alunoId);
            stmt.setObject(3, escolaId);
            stmt.setString(4, tipoVaga);
            stmt.setString(5, serieAno);
            stmt.setString(6, turno);
            stmt.setString(7, protocolo);
            stmt.setBigDecimal(8, pontuacaoTotal != null ? pontuacaoTotal : BigDecimal.ZERO);
            stmt.executeUpdate();
        }
        return inscricaoId;
    }

    public Aluno buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, nome, cpf, data_nascimento, " + COLUNAS_ENDERECO
                + ", renda_per_capita, tem_necessidade_especial "
                + "FROM alunos WHERE id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    public Aluno buscarPorCpf(String cpf) throws SQLException {
        String cpfHash = CpfCryptoService.gerarHash(cpf);
        String sql = "SELECT id, nome, cpf, data_nascimento, " + COLUNAS_ENDERECO
                + ", renda_per_capita, tem_necessidade_especial "
                + "FROM alunos WHERE cpf_hash = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpfHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Aluno> listar() throws SQLException {
        List<Aluno> alunos = new ArrayList<>();
        String sql = "SELECT id, nome, cpf, data_nascimento, " + COLUNAS_ENDERECO
                + ", renda_per_capita, tem_necessidade_especial "
                + "FROM alunos ORDER BY nome ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                alunos.add(mapearResultSet(rs));
            }
        }
        return alunos;
    }

    /**
     * Lista os alunos com inscrição na escola autenticada, permitindo filtro por busca e nível (serieAno).
     */
    public List<AlunoResumo> listarPorEscola(UUID escolaId, String busca, String serieAno) throws SQLException {
        List<AlunoResumo> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT ON (a.id) "
                        + "a.id, a.nome, a.cpf, a.data_nascimento, " + COLUNAS_ENDERECO_ALIASED
                        + ", a.renda_per_capita, a.tem_necessidade_especial, "
                        + "COALESCE(t.serie_ano, i.serie_ano, CASE i.tipo_vaga WHEN 'CRECHE' THEN 'Creche' WHEN 'FUNDAMENTAL' THEN 'Fundamental' ELSE 'Lista de Espera' END) AS etapa_ou_turma, "
                        + "i.tipo_vaga, i.turno, i.pontuacao_total, i.status AS status_inscricao "
                        + "FROM alunos a "
                        + "INNER JOIN inscricoes i ON i.aluno_id = a.id "
                        + "LEFT JOIN turmas t ON t.id = i.turma_id "
                        + "LEFT JOIN aluno_responsaveis ar ON ar.aluno_id = a.id "
                        + "LEFT JOIN responsaveis r ON r.id = ar.responsavel_id "
                        + "WHERE COALESCE(i.escola_id, t.escola_id) = ? ");

        boolean temSerieAno = serieAno != null && !serieAno.trim().isEmpty();
        if (temSerieAno) {
            sql.append("AND LOWER(COALESCE(t.serie_ano, i.serie_ano)) = LOWER(?) ");
        }

        boolean temBusca = busca != null && !busca.trim().isEmpty();
        String digitosBusca = temBusca ? CpfCryptoService.normalizarDigitos(busca) : "";
        boolean buscaPorCpfHash = temBusca && digitosBusca.length() == 11;

        if (buscaPorCpfHash) {
            sql.append("AND (a.cpf_hash = ? OR r.cpf_hash = ?) ");
        } else if (temBusca) {
            sql.append("AND (LOWER(a.nome) LIKE ? OR LOWER(r.nome) LIKE ?) ");
        }

        sql.append("ORDER BY a.id, i.created_at DESC");

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            stmt.setObject(paramIndex++, escolaId);

            if (temSerieAno) {
                stmt.setString(paramIndex++, serieAno.trim());
            }

            if (buscaPorCpfHash) {
                String hash = CpfCryptoService.gerarHash(digitosBusca);
                stmt.setString(paramIndex++, hash);
                stmt.setString(paramIndex++, hash);
            } else if (temBusca) {
                String termoTexto = "%" + busca.trim().toLowerCase() + "%";
                stmt.setString(paramIndex++, termoTexto);
                stmt.setString(paramIndex++, termoTexto);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("data_nascimento");
                    lista.add(new AlunoResumo(
                            (UUID) rs.getObject("id"),
                            rs.getString("nome"),
                            CpfCryptoService.mascararParaExibicao(rs.getString("cpf")),
                            sqlDate != null ? sqlDate.toLocalDate() : null,
                            mapearEndereco(rs),
                            rs.getBigDecimal("renda_per_capita"),
                            rs.getBoolean("tem_necessidade_especial"),
                            rs.getString("etapa_ou_turma"),
                            rs.getString("tipo_vaga"),
                            rs.getString("turno"),
                            rs.getBigDecimal("pontuacao_total"),
                            rs.getString("status_inscricao")));
                }
            }
        }

        lista.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                a.getNome() != null ? a.getNome() : "",
                b.getNome() != null ? b.getNome() : ""));
        return lista;
    }

    public AlunoDetalhe buscarDetalhePorEscola(UUID escolaId, UUID alunoId) throws SQLException {
        return buscarDetalhePorEscola(escolaId, alunoId, false);
    }

    public AlunoDetalhe buscarDetalhePorEscola(
            UUID escolaId,
            UUID alunoId,
            boolean incluirCpfCompleto) throws SQLException {
        String sqlAluno = "SELECT a.id, a.nome, a.cpf, a.data_nascimento, " + COLUNAS_ENDERECO_ALIASED
                + ", a.renda_per_capita, a.tem_necessidade_especial, "
                + "a.vulnerabilidade_socioeconomica, a.responsavel_solo, a.mora_de_aluguel, "
                + "a.bolsa_familia, a.bpc_loas, a.irmao_matriculado, "
                + "i.id AS inscricao_id, i.protocolo, "
                + "COALESCE(t.serie_ano, i.serie_ano, CASE i.tipo_vaga WHEN 'CRECHE' THEN 'Creche' WHEN 'FUNDAMENTAL' THEN 'Fundamental' ELSE 'Lista de Espera' END) AS etapa_ou_turma, "
                + "COALESCE(t.serie_ano, i.serie_ano) AS serie_ano, "
                + "t.nome AS turma_nome, "
                + "i.tipo_vaga, i.turno, i.pontuacao_total, i.status AS status_inscricao, "
                + "i.data_inscricao, i.data_chamada "
                + "FROM alunos a "
                + "INNER JOIN inscricoes i ON i.aluno_id = a.id "
                + "LEFT JOIN turmas t ON t.id = i.turma_id "
                + "WHERE a.id = ? AND COALESCE(i.escola_id, t.escola_id) = ? "
                + "ORDER BY i.created_at DESC LIMIT 1";

        String sqlResponsaveis = "SELECT r.id, r.nome, r.cpf, r.telefone, ar.parentesco, ar.is_contato_principal "
                + "FROM aluno_responsaveis ar "
                + "INNER JOIN responsaveis r ON r.id = ar.responsavel_id "
                + "WHERE ar.aluno_id = ? "
                + "ORDER BY ar.is_contato_principal DESC, r.nome ASC";

        try (Connection conn = Conexao.getConnection()) {
            AlunoDetalhe detalhe = null;
            UUID inscricaoId = null;
            try (PreparedStatement stmt = conn.prepareStatement(sqlAluno)) {
                stmt.setObject(1, alunoId);
                stmt.setObject(2, escolaId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        inscricaoId = (UUID) rs.getObject("inscricao_id");
                        Date sqlDate = rs.getDate("data_nascimento");
                        Timestamp tsInscricao = rs.getTimestamp("data_inscricao");
                        Timestamp tsChamada = rs.getTimestamp("data_chamada");

                        String cpfBruto = rs.getString("cpf");
                        String cpfExibicao = incluirCpfCompleto
                                ? CpfCryptoService.formatar(CpfCryptoService.descriptografar(cpfBruto))
                                : CpfCryptoService.mascararParaExibicao(cpfBruto);

                        detalhe = new AlunoDetalhe(
                                (UUID) rs.getObject("id"),
                                rs.getString("nome"),
                                cpfExibicao,
                                sqlDate != null ? sqlDate.toLocalDate() : null,
                                mapearEndereco(rs),
                                rs.getBigDecimal("renda_per_capita"),
                                rs.getBoolean("tem_necessidade_especial"),
                                rs.getString("protocolo"),
                                rs.getString("etapa_ou_turma"),
                                rs.getString("turma_nome"),
                                rs.getString("tipo_vaga"),
                                rs.getString("turno"),
                                rs.getBigDecimal("pontuacao_total"),
                                rs.getString("status_inscricao"),
                                tsInscricao != null ? tsInscricao.toLocalDateTime() : null,
                                tsChamada != null ? tsChamada.toLocalDateTime() : null,
                                new ArrayList<>());
                        detalhe.setSerieAno(rs.getString("serie_ano"));
                        detalhe.setVulnerabilidadeSocioeconomica(rs.getBoolean("vulnerabilidade_socioeconomica"));
                        detalhe.setResponsavelSolo(rs.getBoolean("responsavel_solo"));
                        detalhe.setMoraDeAluguel(rs.getBoolean("mora_de_aluguel"));
                        detalhe.setBolsaFamilia(rs.getBoolean("bolsa_familia"));
                        detalhe.setBpcLoas(rs.getBoolean("bpc_loas"));
                        detalhe.setIrmaoMatriculado(rs.getBoolean("irmao_matriculado"));
                    }
                }
            }

            if (detalhe == null) {
                return null;
            }

            List<AlunoDetalhe.ResponsavelVinculadoDetalhe> responsaveis = new ArrayList<>();
            try (PreparedStatement stmtResp = conn.prepareStatement(sqlResponsaveis)) {
                stmtResp.setObject(1, alunoId);
                try (ResultSet rsResp = stmtResp.executeQuery()) {
                    while (rsResp.next()) {
                        String cpfRespBruto = rsResp.getString("cpf");
                        String cpfRespExibicao = incluirCpfCompleto
                                ? CpfCryptoService.formatar(CpfCryptoService.descriptografar(cpfRespBruto))
                                : CpfCryptoService.mascararParaExibicao(cpfRespBruto);
                        responsaveis.add(new AlunoDetalhe.ResponsavelVinculadoDetalhe(
                                (UUID) rsResp.getObject("id"),
                                rsResp.getString("nome"),
                                cpfRespExibicao,
                                rsResp.getString("telefone"),
                                rsResp.getString("parentesco"),
                                rsResp.getBoolean("is_contato_principal")));
                    }
                }
            }
            detalhe.setResponsaveis(responsaveis);
            if (inscricaoId != null) {
                InscricaoCriterioDAO inscricaoCriterioDAO = new InscricaoCriterioDAO();
                detalhe.setCriteriosPontuados(inscricaoCriterioDAO.listarDetalhesPorInscricao(conn, inscricaoId));
            }
            return detalhe;
        }
    }

    public void atualizar(Aluno aluno, boolean atualizarCpf, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "UPDATE alunos SET nome = ?, data_nascimento = ?, "
                        + "cep = ?, rua = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, estado = ?, "
                        + "renda_per_capita = ?, tem_necessidade_especial = ?, "
                        + "vulnerabilidade_socioeconomica = ?, responsavel_solo = ?, mora_de_aluguel = ?, "
                        + "bolsa_familia = ?, bpc_loas = ?, irmao_matriculado = ? ");
        if (atualizarCpf) {
            sql.append(", cpf = ?, cpf_hash = ? ");
        }
        sql.append("WHERE id = ?");

        Endereco endereco = aluno.getEndereco();
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setString(idx++, aluno.getNome());
            stmt.setDate(idx++, aluno.getDataNascimento() != null ? Date.valueOf(aluno.getDataNascimento()) : null);
            stmt.setString(idx++, endereco != null ? endereco.getCep() : null);
            stmt.setString(idx++, endereco != null ? endereco.getRua() : null);
            stmt.setString(idx++, endereco != null ? endereco.getNumero() : null);
            stmt.setString(idx++, endereco != null ? endereco.getComplemento() : null);
            stmt.setString(idx++, endereco != null ? endereco.getBairro() : null);
            stmt.setString(idx++, endereco != null ? endereco.getCidade() : null);
            stmt.setString(idx++, endereco != null ? endereco.getEstado() : null);
            stmt.setBigDecimal(idx++, aluno.getRendaPerCapita() != null ? aluno.getRendaPerCapita() : BigDecimal.ZERO);
            stmt.setBoolean(idx++, aluno.isTemNecessidadeEspecial());
            stmt.setBoolean(idx++, aluno.isVulnerabilidadeSocioeconomica());
            stmt.setBoolean(idx++, aluno.isResponsavelSolo());
            stmt.setBoolean(idx++, aluno.isMoraDeAluguel());
            stmt.setBoolean(idx++, aluno.isBolsaFamilia());
            stmt.setBoolean(idx++, aluno.isBpcLoas());
            stmt.setBoolean(idx++, aluno.isIrmaoMatriculado());
            if (atualizarCpf) {
                stmt.setString(idx++, CpfCryptoService.criptografar(aluno.getCpf()));
                stmt.setString(idx++, CpfCryptoService.gerarHash(aluno.getCpf()));
            }
            stmt.setObject(idx, aluno.getId());
            stmt.executeUpdate();
        }
    }

    public void removerResponsaveis(UUID alunoId, Connection conn) throws SQLException {
        String sql = "DELETE FROM aluno_responsaveis WHERE aluno_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, alunoId);
            stmt.executeUpdate();
        }
    }

    public UUID atualizarInscricaoEscola(
            Connection conn,
            UUID escolaId,
            UUID alunoId,
            String tipoVaga,
            String serieAno,
            String turno,
            BigDecimal pontuacaoTotal) throws SQLException {
        String sql = "UPDATE inscricoes SET tipo_vaga = ?, serie_ano = ?, turno = ?, pontuacao_total = ? "
                + "WHERE id = ("
                + "  SELECT i.id FROM inscricoes i "
                + "  LEFT JOIN turmas t ON t.id = i.turma_id "
                + "  WHERE i.aluno_id = ? AND COALESCE(i.escola_id, t.escola_id) = ? "
                + "  ORDER BY i.created_at DESC LIMIT 1"
                + ") RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipoVaga);
            stmt.setString(2, serieAno);
            stmt.setString(3, turno);
            stmt.setBigDecimal(4, pontuacaoTotal != null ? pontuacaoTotal : BigDecimal.ZERO);
            stmt.setObject(5, alunoId);
            stmt.setObject(6, escolaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
            }
        }
        return null;
    }

    private Aluno mapearResultSet(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("data_nascimento");
        LocalDate dataNascimento = sqlDate != null ? sqlDate.toLocalDate() : null;

        return new Aluno(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                CpfCryptoService.mascararParaExibicao(rs.getString("cpf")),
                dataNascimento,
                mapearEndereco(rs),
                rs.getBigDecimal("renda_per_capita"),
                rs.getBoolean("tem_necessidade_especial"));
    }

    private Endereco mapearEndereco(ResultSet rs) throws SQLException {
        String cep = rs.getString("cep");
        String rua = rs.getString("rua");
        String numero = rs.getString("numero");
        String complemento = rs.getString("complemento");
        String bairro = rs.getString("bairro");
        String cidade = rs.getString("cidade");
        String estado = rs.getString("estado");

        if (cep == null && rua == null && numero == null
                && complemento == null && bairro == null && cidade == null && estado == null) {
            return null;
        }

        return new Endereco(cep, rua, numero, complemento, bairro, cidade, estado);
    }
}
