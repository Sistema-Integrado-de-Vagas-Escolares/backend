package com.sive.dao;

import com.sive.model.Responsavel;
import com.sive.model.ResponsavelDetalhe;
import com.sive.model.ResponsavelResumo;
import com.sive.security.CpfCryptoService;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResponsavelDAO {

    public Responsavel inserir(Responsavel responsavel) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return inserir(responsavel, conn);
        }
    }

    public Responsavel inserir(Responsavel responsavel, Connection conn) throws SQLException {
        String sql = "INSERT INTO responsaveis (id, nome, cpf, cpf_hash, telefone) VALUES (?, ?, ?, ?, ?)";

        if (responsavel.getId() == null) {
            responsavel.setId(UUID.randomUUID());
        }

        String cpfOriginal = responsavel.getCpf();
        String cpfCriptografado = CpfCryptoService.criptografar(cpfOriginal);
        String cpfHash = CpfCryptoService.gerarHash(cpfOriginal);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, responsavel.getId());
            stmt.setString(2, responsavel.getNome());
            stmt.setString(3, cpfCriptografado);
            stmt.setString(4, cpfHash);
            stmt.setString(5, responsavel.getTelefone());

            stmt.executeUpdate();
            responsavel.setCpf(CpfCryptoService.mascararParaExibicao(cpfCriptografado));
            return responsavel;
        }
    }

    public void atualizar(Responsavel responsavel, boolean atualizarCpf) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE responsaveis SET nome = ?, telefone = ? ");
        if (atualizarCpf) {
            sql.append(", cpf = ?, cpf_hash = ? ");
        }
        sql.append("WHERE id = ?");

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setString(idx++, responsavel.getNome());
            stmt.setString(idx++, responsavel.getTelefone());
            if (atualizarCpf) {
                stmt.setString(idx++, CpfCryptoService.criptografar(responsavel.getCpf()));
                stmt.setString(idx++, CpfCryptoService.gerarHash(responsavel.getCpf()));
            }
            stmt.setObject(idx, responsavel.getId());
            stmt.executeUpdate();
        }
    }

    public Responsavel buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone FROM responsaveis WHERE id = ?";

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

    public Responsavel buscarPorCpf(String cpf) throws SQLException {
        String cpfHash = CpfCryptoService.gerarHash(cpf);
        String sql = "SELECT id, nome, cpf, telefone FROM responsaveis WHERE cpf_hash = ?";

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

    public List<Responsavel> listar() throws SQLException {
        List<Responsavel> responsaveis = new ArrayList<>();
        String sql = "SELECT id, nome, cpf, telefone FROM responsaveis ORDER BY nome ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                responsaveis.add(mapearResultSet(rs));
            }
        }
        return responsaveis;
    }

    /**
     * Lista responsáveis com a contagem de filhos vinculados e filtros opcionais de busca e vínculo.
     * Quando a busca contém um CPF completo (11 dígitos), consulta pelo cpf_hash.
     */
    public List<ResponsavelResumo> listarComTotalFilhos(String busca, Boolean comFilhos) throws SQLException {
        List<ResponsavelResumo> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.nome, r.cpf, r.telefone, "
                        + "COUNT(DISTINCT ar.aluno_id) AS total_filhos "
                        + "FROM responsaveis r "
                        + "LEFT JOIN aluno_responsaveis ar ON ar.responsavel_id = r.id ");

        boolean temBusca = busca != null && !busca.trim().isEmpty();
        String digitosBusca = temBusca ? CpfCryptoService.normalizarDigitos(busca) : "";
        boolean buscaPorCpfHash = temBusca && digitosBusca.length() == 11;

        if (buscaPorCpfHash) {
            sql.append("WHERE r.cpf_hash = ? ");
        } else if (temBusca) {
            sql.append("WHERE LOWER(r.nome) LIKE ? ");
        }

        sql.append("GROUP BY r.id, r.nome, r.cpf, r.telefone ");

        if (Boolean.TRUE.equals(comFilhos)) {
            sql.append("HAVING COUNT(DISTINCT ar.aluno_id) > 0 ");
        } else if (Boolean.FALSE.equals(comFilhos)) {
            sql.append("HAVING COUNT(DISTINCT ar.aluno_id) = 0 ");
        }

        sql.append("ORDER BY r.nome ASC");

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (buscaPorCpfHash) {
                stmt.setString(1, CpfCryptoService.gerarHash(digitosBusca));
            } else if (temBusca) {
                stmt.setString(1, "%" + busca.trim().toLowerCase() + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResponsavelResumo(
                            (UUID) rs.getObject("id"),
                            rs.getString("nome"),
                            CpfCryptoService.mascararParaExibicao(rs.getString("cpf")),
                            rs.getString("telefone"),
                            rs.getInt("total_filhos")));
                }
            }
        }
        return lista;
    }

    public ResponsavelDetalhe buscarDetalhePorEscola(UUID escolaId, UUID responsavelId) throws SQLException {
        return buscarDetalhePorEscola(escolaId, responsavelId, false);
    }

    public ResponsavelDetalhe buscarDetalhePorEscola(
            UUID escolaId,
            UUID responsavelId,
            boolean incluirCpfCompleto) throws SQLException {
        String sqlResp = "SELECT id, nome, cpf, telefone FROM responsaveis WHERE id = ?";
        String sqlFilhos = "SELECT DISTINCT ON (a.id) "
                + "a.id, a.nome, a.cpf, a.data_nascimento, "
                + "COALESCE(t.serie_ano, i.serie_ano, CASE i.tipo_vaga WHEN 'CRECHE' THEN 'Creche' WHEN 'FUNDAMENTAL' THEN 'Fundamental' ELSE 'Sem inscrição' END) AS etapa_ou_turma, "
                + "t.nome AS turma_nome, "
                + "i.status AS status_inscricao, "
                + "i.pontuacao_total, "
                + "ar.parentesco, "
                + "ar.is_contato_principal "
                + "FROM aluno_responsaveis ar "
                + "INNER JOIN alunos a ON a.id = ar.aluno_id "
                + "LEFT JOIN inscricoes i ON i.aluno_id = a.id "
                + "LEFT JOIN turmas t ON t.id = i.turma_id "
                + "WHERE ar.responsavel_id = ? "
                + "AND (i.id IS NULL OR COALESCE(i.escola_id, t.escola_id) = ?) "
                + "ORDER BY a.id, i.created_at DESC NULLS LAST";

        try (Connection conn = Conexao.getConnection()) {
            ResponsavelDetalhe detalhe = null;
            try (PreparedStatement stmt = conn.prepareStatement(sqlResp)) {
                stmt.setObject(1, responsavelId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String cpfBruto = rs.getString("cpf");
                        String cpfExibicao = incluirCpfCompleto
                                ? CpfCryptoService.formatar(CpfCryptoService.descriptografar(cpfBruto))
                                : CpfCryptoService.mascararParaExibicao(cpfBruto);

                        detalhe = new ResponsavelDetalhe(
                                (UUID) rs.getObject("id"),
                                rs.getString("nome"),
                                cpfExibicao,
                                rs.getString("telefone"),
                                new ArrayList<>());
                    }
                }
            }

            if (detalhe == null) {
                return null;
            }

            List<ResponsavelDetalhe.FilhoVinculadoResumo> filhos = new ArrayList<>();
            try (PreparedStatement stmtFilhos = conn.prepareStatement(sqlFilhos)) {
                stmtFilhos.setObject(1, responsavelId);
                stmtFilhos.setObject(2, escolaId);
                try (ResultSet rsFilhos = stmtFilhos.executeQuery()) {
                    while (rsFilhos.next()) {
                        Date sqlDate = rsFilhos.getDate("data_nascimento");
                        String cpfFilhoBruto = rsFilhos.getString("cpf");
                        String cpfFilhoExibicao = incluirCpfCompleto
                                ? CpfCryptoService.formatar(CpfCryptoService.descriptografar(cpfFilhoBruto))
                                : CpfCryptoService.mascararParaExibicao(cpfFilhoBruto);
                        filhos.add(new ResponsavelDetalhe.FilhoVinculadoResumo(
                                (UUID) rsFilhos.getObject("id"),
                                rsFilhos.getString("nome"),
                                cpfFilhoExibicao,
                                sqlDate != null ? sqlDate.toLocalDate() : null,
                                rsFilhos.getString("etapa_ou_turma"),
                                rsFilhos.getString("turma_nome"),
                                rsFilhos.getString("status_inscricao"),
                                rsFilhos.getBigDecimal("pontuacao_total"),
                                rsFilhos.getString("parentesco"),
                                rsFilhos.getBoolean("is_contato_principal")));
                    }
                }
            }
            filhos.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                    a.getNome() != null ? a.getNome() : "",
                    b.getNome() != null ? b.getNome() : ""));
            detalhe.setFilhos(filhos);
            return detalhe;
        }
    }

    private Responsavel mapearResultSet(ResultSet rs) throws SQLException {
        return new Responsavel(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                CpfCryptoService.mascararParaExibicao(rs.getString("cpf")),
                rs.getString("telefone"));
    }
}
