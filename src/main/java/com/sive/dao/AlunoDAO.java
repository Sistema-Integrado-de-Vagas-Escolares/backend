package com.sive.dao;

import com.sive.model.Aluno;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlunoDAO {

    public Aluno inserir(Aluno aluno) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return inserir(aluno, conn);
        }
    }

    public Aluno inserir(Aluno aluno, Connection conn) throws SQLException {
        String sql = "INSERT INTO alunos (id, nome, data_nascimento, certidao_nascimento, tem_necessidade_especial) "
                + "VALUES (?, ?, ?, ?, ?)";

        if (aluno.getId() == null) {
            aluno.setId(UUID.randomUUID());
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, aluno.getId());
            stmt.setString(2, aluno.getNome());
            stmt.setDate(3, aluno.getDataNascimento() != null ? Date.valueOf(aluno.getDataNascimento()) : null);
            stmt.setString(4, aluno.getCertidaoNascimento());
            stmt.setBoolean(5, aluno.isTemNecessidadeEspecial());

            stmt.executeUpdate();
            return aluno;
        }
    }

    public Aluno buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, nome, data_nascimento, certidao_nascimento, tem_necessidade_especial "
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

    public Aluno buscarPorCertidao(String certidaoNascimento) throws SQLException {
        String sql = "SELECT id, nome, data_nascimento, certidao_nascimento, tem_necessidade_especial "
                + "FROM alunos WHERE certidao_nascimento = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, certidaoNascimento);

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
        String sql = "SELECT id, nome, data_nascimento, certidao_nascimento, tem_necessidade_especial "
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

    private Aluno mapearResultSet(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("data_nascimento");
        LocalDate dataNascimento = sqlDate != null ? sqlDate.toLocalDate() : null;

        return new Aluno(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                dataNascimento,
                rs.getString("certidao_nascimento"),
                rs.getBoolean("tem_necessidade_especial"));
    }
}
