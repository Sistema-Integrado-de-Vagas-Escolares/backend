package com.sive.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sive.model.Turma;
import com.sive.model.Turno;

/**
 * Data Access Object (DAO) para a entidade Turma utilizando JDBC.
 */
public class TurmaDao {

    /**
     * Insere uma nova turma no banco de dados.
     */
    public Turma inserir(Turma turma) throws SQLException {
        String sql = "INSERT INTO turmas (id, escola_id, serie_ano, turno, vagas_totais, vagas_disponiveis) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING created_at";

        if (turma.getId() == null) {
            turma.setId(UUID.randomUUID());
        }

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, turma.getId());
            stmt.setObject(2, turma.getEscolaId());
            stmt.setString(3, turma.getSerieAno());
            stmt.setString(4, turma.getTurno().name());
            stmt.setInt(5, turma.getVagasTotais());
            stmt.setInt(6, turma.getVagasDisponiveis());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        turma.setDataCriacao(createdAt.toLocalDateTime());
                    }
                }
            }
            return turma;
        }
    }

    /**
     * Retorna a lista de todas as turmas de uma escola ordenadas por serie/ano.
     */
    public List<Turma> listarPorEscola(UUID escolaId) throws SQLException {
        List<Turma> turmas = new ArrayList<>();
        String sql = "SELECT id, escola_id, serie_ano, turno, vagas_totais, vagas_disponiveis, created_at "
                + "FROM turmas WHERE escola_id = ? ORDER BY serie_ano ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, escolaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String turnoStr = rs.getString("turno");
                    Turno turno = turnoStr != null ? Turno.valueOf(turnoStr) : null;

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    LocalDateTime dataCriacao = createdAt != null ? createdAt.toLocalDateTime() : null;

                    Turma turma = new Turma(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("escola_id"),
                            rs.getString("serie_ano"),
                            turno,
                            rs.getInt("vagas_totais"),
                            rs.getInt("vagas_disponiveis"),
                            dataCriacao);
                    turmas.add(turma);
                }
            }
        }
        return turmas;
    }

    /**
     * Busca uma turma pelo seu identificador unico (UUID).
     */
    public Turma buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, escola_id, serie_ano, turno, vagas_totais, vagas_disponiveis, created_at FROM escolas WHERE id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String turnoStr = rs.getString("turno");
                    Turno turno = turnoStr != null ? Turno.valueOf(turnoStr) : null;

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    LocalDateTime dataCriacao = createdAt != null ? createdAt.toLocalDateTime() : null;

                    return new Turma(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("escola_id"),
                            rs.getString("serie_ano"),
                            turno,
                            rs.getInt("vagas_totais"),
                            rs.getInt("vagas_disponiveis"),
                            dataCriacao);
                }
            }
        }
        return null;
    }

    /**
     * Atualiza o saldo de vagas de uma turma.
     */
    public void atualizarSaldoVagas(UUID id, int quantidade) throws SQLException {
        String sql = "UPDATE turmas SET vagas_disponiveis = vagas_disponiveis + ? WHERE id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantidade);
            stmt.setObject(2, id);
            stmt.executeUpdate();
        }
    }
}
