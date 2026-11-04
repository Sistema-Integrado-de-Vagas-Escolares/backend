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
 * Data Access Object (DAO) para a entidade Escola utilizando JDBC puro.
 */
public class EscolaDAO {

    /**
     * Insere uma nova escola no banco de dados.
     */
    public Escola inserir(Escola escola) throws SQLException {
        String sql = "INSERT INTO escolas (id, nome, bairro, cidade, capacidade_total) VALUES (?, ?, ?, ?, ?)";

        if (escola.getId() == null) {
            escola.setId(UUID.randomUUID());
        }

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, escola.getId());
            stmt.setString(2, escola.getNome());
            stmt.setString(3, escola.getBairro());
            stmt.setString(4, escola.getCidade());
            stmt.setInt(5, escola.getCapacidadeTotal());

            stmt.executeUpdate();
            return escola;
        }
    }

    /**
     * Retorna a lista de todas as escolas cadastradas ordenadas por nome.
     */
    public List<Escola> listar() throws SQLException {
        List<Escola> escolas = new ArrayList<>();
        String sql = "SELECT id, nome, bairro, cidade, capacidade_total FROM escolas ORDER BY nome ASC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Escola escola = new Escola(
                        (UUID) rs.getObject("id"),
                        rs.getString("nome"),
                        rs.getString("bairro"),
                        rs.getString("cidade"),
                        rs.getInt("capacidade_total")
                );
                escolas.add(escola);
            }
        }
        return escolas;
    }

    /**
     * Busca uma escola pelo seu identificador unico (UUID).
     */
    public Escola buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, nome, bairro, cidade, capacidade_total FROM escolas WHERE id = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Escola(
                            (UUID) rs.getObject("id"),
                            rs.getString("nome"),
                            rs.getString("bairro"),
                            rs.getString("cidade"),
                            rs.getInt("capacidade_total")
                    );
                }
            }
        }
        return null;
    }
}
