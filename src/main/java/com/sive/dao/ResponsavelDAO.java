package com.sive.dao;

import com.sive.model.Responsavel;

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
        String sql = "INSERT INTO responsaveis (id, nome, cpf, telefone, renda_familiar) VALUES (?, ?, ?, ?, ?)";

        if (responsavel.getId() == null) {
            responsavel.setId(UUID.randomUUID());
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, responsavel.getId());
            stmt.setString(2, responsavel.getNome());
            stmt.setString(3, responsavel.getCpf());
            stmt.setString(4, responsavel.getTelefone());
            stmt.setBigDecimal(5, responsavel.getRendaFamiliar());

            stmt.executeUpdate();
            return responsavel;
        }
    }

    public Responsavel buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone, renda_familiar FROM responsaveis WHERE id = ?";

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
        String sql = "SELECT id, nome, cpf, telefone, renda_familiar FROM responsaveis WHERE cpf = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);

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
        String sql = "SELECT id, nome, cpf, telefone, renda_familiar FROM responsaveis ORDER BY nome ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                responsaveis.add(mapearResultSet(rs));
            }
        }
        return responsaveis;
    }

    private Responsavel mapearResultSet(ResultSet rs) throws SQLException {
        return new Responsavel(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                rs.getBigDecimal("renda_familiar"));
    }
}
