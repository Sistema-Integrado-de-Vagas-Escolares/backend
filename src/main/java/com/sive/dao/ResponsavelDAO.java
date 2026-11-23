package com.sive.dao;

import com.sive.model.Responsavel;
import com.sive.model.ResponsavelResumo;
import com.sive.security.CpfCryptoService;

import java.sql.Connection;
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

    private Responsavel mapearResultSet(ResultSet rs) throws SQLException {
        return new Responsavel(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                CpfCryptoService.mascararParaExibicao(rs.getString("cpf")),
                rs.getString("telefone"));
    }
}
