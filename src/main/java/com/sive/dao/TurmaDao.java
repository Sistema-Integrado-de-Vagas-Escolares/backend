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
        String sql = "INSERT INTO turmas (id, escola_id, serie_ano, nome, turno, vagas_totais, vagas_disponiveis) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING created_at";

        if (turma.getId() == null) {
            turma.setId(UUID.randomUUID());
        }

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, turma.getId());
            stmt.setObject(2, turma.getEscolaId());
            stmt.setString(3, turma.getSerieAno());
            stmt.setString(4, turma.getNome());
            stmt.setString(5, turma.getTurno().name());
            stmt.setInt(6, turma.getVagasTotais());
            stmt.setInt(7, turma.getVagasDisponiveis());

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
     * Retorna a lista de todas as turmas de uma escola ordenadas por série/ano.
     */
    public List<Turma> listarPorEscola(UUID escolaId) throws SQLException {
        List<Turma> turmas = new ArrayList<>();
        String sql = "SELECT id, escola_id, serie_ano, nome, turno, vagas_totais, vagas_disponiveis, created_at "
                + "FROM turmas WHERE escola_id = ? ORDER BY serie_ano ASC, nome ASC";

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
                            rs.getString("nome"),
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
     * Busca uma turma pelo seu identificador único (UUID).
     */
    public Turma buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, escola_id, serie_ano, nome, turno, vagas_totais, vagas_disponiveis, created_at FROM turmas WHERE id = ?";

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
                            rs.getString("nome"),
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
     * Atualiza o saldo de vagas disponíveis de uma turma para um valor específico.
     */
    public void atualizarSaldoVagas(UUID id, int novoSaldo) throws SQLException {
        String sql = "UPDATE turmas SET vagas_disponiveis = ? WHERE id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novoSaldo);
            stmt.setObject(2, id);
            stmt.executeUpdate();
        }
    }

    public Turma atualizar(Turma turma) throws SQLException {
        String sql = "UPDATE turmas SET serie_ano = ?, nome = ?, turno = ?, vagas_totais = ?, vagas_disponiveis = ? "
                + "WHERE id = ? AND escola_id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, turma.getSerieAno());
            stmt.setString(2, turma.getNome());
            stmt.setString(3, turma.getTurno().name());
            stmt.setInt(4, turma.getVagasTotais());
            stmt.setInt(5, turma.getVagasDisponiveis());
            stmt.setObject(6, turma.getId());
            stmt.setObject(7, turma.getEscolaId());
            stmt.executeUpdate();
        }
        return buscarPorId(turma.getId());
    }

    public int contarInscricoesVinculadas(UUID turmaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscricoes WHERE turma_id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, turmaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void excluir(UUID escolaId, UUID turmaId) throws SQLException {
        String sql = "DELETE FROM turmas WHERE id = ? AND escola_id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, turmaId);
            stmt.setObject(2, escolaId);
            stmt.executeUpdate();
        }
    }
}
