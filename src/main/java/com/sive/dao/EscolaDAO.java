package com.sive.dao;

import com.sive.model.Escola;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) para a entidade Escola utilizando JDBC.
 */
public class EscolaDAO {

    /**
     * Insere uma nova escola no banco de dados.
     */
    public Escola inserir(Escola escola) throws SQLException {
        String sql = "INSERT INTO escolas (id, codigo, nome, bairro, cidade, capacidade_total, senha_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        if (escola.getId() == null) {
            escola.setId(UUID.randomUUID());
        }

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, escola.getId());
            stmt.setString(2, escola.getCodigo());
            stmt.setString(3, escola.getNome());
            stmt.setString(4, escola.getBairro());
            stmt.setString(5, escola.getCidade());
            stmt.setInt(6, escola.getCapacidadeTotal());
            stmt.setString(7, escola.getSenhaHash());

            stmt.executeUpdate();
            return escola;
        }
    }

    /**
     * Retorna a lista de todas as escolas cadastradas ordenadas por nome.
     */
    public List<Escola> listar() throws SQLException {
        List<Escola> escolas = new ArrayList<>();
        String sql = "SELECT id, codigo, nome, bairro, cidade, capacidade_total FROM escolas ORDER BY nome ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                escolas.add(mapearEscolaSemSenha(rs));
            }
        }
        return escolas;
    }

    /**
     * Busca uma escola pelo seu identificador único (UUID).
     */
    public Escola buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, codigo, nome, bairro, cidade, capacidade_total FROM escolas WHERE id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEscolaSemSenha(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca uma escola pelo código identificador (ex: EMEI-0012), incluindo o hash da senha para autenticação.
     */
    public Escola buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT id, codigo, nome, bairro, cidade, capacidade_total, senha_hash FROM escolas WHERE codigo = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Escola(
                            (UUID) rs.getObject("id"),
                            rs.getString("codigo"),
                            rs.getString("nome"),
                            rs.getString("bairro"),
                            rs.getString("cidade"),
                            rs.getInt("capacidade_total"),
                            rs.getString("senha_hash"));
                }
            }
        }
        return null;
    }

    /**
     * Verifica se já existe uma escola cadastrada com o código informado.
     */
    public boolean existePorCodigo(String codigo) throws SQLException {
        String sql = "SELECT 1 FROM escolas WHERE codigo = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Escola mapearEscolaSemSenha(ResultSet rs) throws SQLException {
        return new Escola(
                (UUID) rs.getObject("id"),
                rs.getString("codigo"),
                rs.getString("nome"),
                rs.getString("bairro"),
                rs.getString("cidade"),
                rs.getInt("capacidade_total"));
    }
}
