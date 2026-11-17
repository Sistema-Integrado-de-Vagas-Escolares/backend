package com.sive.dao;

import com.sive.model.AlunoResponsavel;
import com.sive.model.Parentesco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlunoResponsavelDAO {

    public AlunoResponsavel vincular(AlunoResponsavel vinculo) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return vincular(vinculo, conn);
        }
    }

    public AlunoResponsavel vincular(AlunoResponsavel vinculo, Connection conn) throws SQLException {
        String sql = "INSERT INTO aluno_responsaveis (id, aluno_id, responsavel_id, parentesco, is_contato_principal) "
                + "VALUES (?, ?, ?, ?, ?)";

        if (vinculo.getId() == null) {
            vinculo.setId(UUID.randomUUID());
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, vinculo.getId());
            stmt.setObject(2, vinculo.getAlunoId());
            stmt.setObject(3, vinculo.getResponsavelId());
            stmt.setString(4, vinculo.getParentesco() != null ? vinculo.getParentesco().name() : Parentesco.MAE.name());
            stmt.setBoolean(5, vinculo.isContatoPrincipal());

            stmt.executeUpdate();
            return vinculo;
        }
    }

    public AlunoResponsavel buscarPorAlunoEResponsavel(UUID alunoId, UUID responsavelId) throws SQLException {
        String sql = "SELECT id, aluno_id, responsavel_id, parentesco, is_contato_principal "
                + "FROM aluno_responsaveis WHERE aluno_id = ? AND responsavel_id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, alunoId);
            stmt.setObject(2, responsavelId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<AlunoResponsavel> listarPorAluno(UUID alunoId) throws SQLException {
        List<AlunoResponsavel> vinculos = new ArrayList<>();
        String sql = "SELECT id, aluno_id, responsavel_id, parentesco, is_contato_principal "
                + "FROM aluno_responsaveis WHERE aluno_id = ? ORDER BY is_contato_principal DESC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, alunoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vinculos.add(mapearResultSet(rs));
                }
            }
        }
        return vinculos;
    }

    public List<AlunoResponsavel> listarPorResponsavel(UUID responsavelId) throws SQLException {
        List<AlunoResponsavel> vinculos = new ArrayList<>();
        String sql = "SELECT id, aluno_id, responsavel_id, parentesco, is_contato_principal "
                + "FROM aluno_responsaveis WHERE responsavel_id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, responsavelId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vinculos.add(mapearResultSet(rs));
                }
            }
        }
        return vinculos;
    }

    public AlunoResponsavel buscarContatoPrincipal(UUID alunoId) throws SQLException {
        String sql = "SELECT id, aluno_id, responsavel_id, parentesco, is_contato_principal "
                + "FROM aluno_responsaveis WHERE aluno_id = ? AND is_contato_principal = TRUE";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, alunoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    public void desvincular(UUID alunoId, UUID responsavelId) throws SQLException {
        String sql = "DELETE FROM aluno_responsaveis WHERE aluno_id = ? AND responsavel_id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, alunoId);
            stmt.setObject(2, responsavelId);
            stmt.executeUpdate();
        }
    }

    private AlunoResponsavel mapearResultSet(ResultSet rs) throws SQLException {
        return new AlunoResponsavel(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("aluno_id"),
                (UUID) rs.getObject("responsavel_id"),
                Parentesco.fromString(rs.getString("parentesco")),
                rs.getBoolean("is_contato_principal"));
    }
}
