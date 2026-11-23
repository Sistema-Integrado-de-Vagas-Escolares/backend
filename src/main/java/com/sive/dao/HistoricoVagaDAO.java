package com.sive.dao;

import com.sive.model.HistoricoVaga;
import com.sive.model.Turno;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO JDBC puro para atualização de vagas disponíveis e registro de histórico.
 */
@Repository
public class HistoricoVagaDAO {

    public List<HistoricoVaga> listarPorEscola(UUID escolaId, UUID turmaId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT h.id, h.escola_id, h.turma_id, t.serie_ano, t.nome AS turma_nome, t.turno, "
                        + "h.vagas_anteriores, h.vagas_novas, h.motivo, h.created_at "
                        + "FROM historico_vagas h "
                        + "INNER JOIN turmas t ON t.id = h.turma_id "
                        + "WHERE h.escola_id = ? ");
        if (turmaId != null) {
            sql.append("AND h.turma_id = ? ");
        }
        sql.append("ORDER BY h.created_at DESC");

        List<HistoricoVaga> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setObject(1, escolaId);
            if (turmaId != null) {
                stmt.setObject(2, turmaId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String turnoStr = rs.getString("turno");
                    Timestamp ts = rs.getTimestamp("created_at");
                    lista.add(new HistoricoVaga(
                            rs.getObject("id", UUID.class),
                            rs.getObject("escola_id", UUID.class),
                            rs.getObject("turma_id", UUID.class),
                            rs.getString("serie_ano"),
                            rs.getString("turma_nome"),
                            turnoStr != null ? Turno.valueOf(turnoStr) : null,
                            rs.getInt("vagas_anteriores"),
                            rs.getInt("vagas_novas"),
                            rs.getString("motivo"),
                            ts != null ? ts.toLocalDateTime() : null));
                }
            }
        }
        return lista;
    }

    public HistoricoVaga atualizarVagasERegistrarHistorico(
            UUID escolaId,
            UUID turmaId,
            int novasVagas,
            String motivo) throws SQLException {
        String sqlSelectTurma = "SELECT serie_ano, nome, turno, vagas_totais, vagas_disponiveis "
                + "FROM turmas WHERE id = ? AND escola_id = ? FOR UPDATE";
        String sqlUpdateTurma = "UPDATE turmas SET vagas_disponiveis = ? WHERE id = ? AND escola_id = ?";
        String sqlInsertHistorico = "INSERT INTO historico_vagas "
                + "(id, escola_id, turma_id, vagas_anteriores, vagas_novas, motivo) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING created_at";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String serieAno;
                String turmaNome;
                Turno turno;
                int vagasTotais;
                int vagasAnteriores;

                try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelectTurma)) {
                    stmtSelect.setObject(1, turmaId);
                    stmtSelect.setObject(2, escolaId);
                    try (ResultSet rs = stmtSelect.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Turma não encontrada para esta escola.");
                        }
                        serieAno = rs.getString("serie_ano");
                        turmaNome = rs.getString("nome");
                        String turnoStr = rs.getString("turno");
                        turno = turnoStr != null ? Turno.valueOf(turnoStr) : null;
                        vagasTotais = rs.getInt("vagas_totais");
                        vagasAnteriores = rs.getInt("vagas_disponiveis");
                    }
                }

                if (novasVagas < 0 || novasVagas > vagasTotais) {
                    throw new IllegalArgumentException(
                            "A quantidade de vagas disponíveis deve estar entre 0 e " + vagasTotais + ".");
                }

                if (novasVagas == vagasAnteriores) {
                    throw new IllegalArgumentException(
                            "Informe uma quantidade diferente das " + vagasAnteriores + " vagas atuais.");
                }

                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateTurma)) {
                    stmtUpdate.setInt(1, novasVagas);
                    stmtUpdate.setObject(2, turmaId);
                    stmtUpdate.setObject(3, escolaId);
                    stmtUpdate.executeUpdate();
                }

                UUID historicoId = UUID.randomUUID();
                Timestamp createdAt = null;
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertHistorico)) {
                    stmtInsert.setObject(1, historicoId);
                    stmtInsert.setObject(2, escolaId);
                    stmtInsert.setObject(3, turmaId);
                    stmtInsert.setInt(4, vagasAnteriores);
                    stmtInsert.setInt(5, novasVagas);
                    stmtInsert.setString(6, motivo);
                    try (ResultSet rs = stmtInsert.executeQuery()) {
                        if (rs.next()) {
                            createdAt = rs.getTimestamp("created_at");
                        }
                    }
                }

                conn.commit();
                return new HistoricoVaga(
                        historicoId,
                        escolaId,
                        turmaId,
                        serieAno,
                        turmaNome,
                        turno,
                        vagasAnteriores,
                        novasVagas,
                        motivo,
                        createdAt != null ? createdAt.toLocalDateTime() : null);
            } catch (RuntimeException | SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
