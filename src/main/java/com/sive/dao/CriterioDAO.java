package com.sive.dao;

import com.sive.model.Criterio;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO JDBC puro para gerenciamento dos critérios de pontuação da fila de espera.
 */
@Repository
public class CriterioDAO {

    public List<Criterio> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, descricao, pontos_base, categoria, tipo_resposta, ativo, ordem, created_at "
                + "FROM criterios "
                + "ORDER BY ordem ASC, created_at ASC";

        List<Criterio> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                lista.add(new Criterio(
                        rs.getObject("id", UUID.class),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getBigDecimal("pontos_base").doubleValue(),
                        rs.getString("categoria"),
                        rs.getString("tipo_resposta"),
                        rs.getBoolean("ativo"),
                        rs.getInt("ordem"),
                        ts != null ? ts.toLocalDateTime() : null));
            }
        }
        return lista;
    }

    public List<Criterio> substituirTodos(List<Criterio> criterios) throws SQLException {
        String sqlDeleteRemoved = "DELETE FROM criterios WHERE id = ?";
        String sqlUpdate = "UPDATE criterios SET nome = ?, descricao = ?, pontos_base = ?, categoria = ?, "
                + "tipo_resposta = ?, ativo = ?, ordem = ? WHERE id = ?";
        String sqlInsert = "INSERT INTO criterios (id, nome, descricao, pontos_base, categoria, tipo_resposta, ativo, ordem) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<UUID> existentes = listarIdsExistentes(conn);
                List<UUID> mantidos = new ArrayList<>();
                for (Criterio criterio : criterios) {
                    if (criterio.getId() != null) {
                        mantidos.add(criterio.getId());
                    }
                }

                try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteRemoved)) {
                    for (UUID idExistente : existentes) {
                        if (!mantidos.contains(idExistente)) {
                            stmtDelete.setObject(1, idExistente);
                            stmtDelete.executeUpdate();
                        }
                    }
                }

                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
                        PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                    for (int i = 0; i < criterios.size(); i++) {
                        Criterio c = criterios.get(i);
                        int ordem = i + 1;
                        if (c.getId() != null && existentes.contains(c.getId())) {
                            stmtUpdate.setString(1, c.getNome());
                            stmtUpdate.setString(2, c.getDescricao());
                            stmtUpdate.setBigDecimal(3, BigDecimal.valueOf(c.getPontosBase()));
                            stmtUpdate.setString(4, c.getCategoria());
                            stmtUpdate.setString(5, c.getTipoResposta());
                            stmtUpdate.setBoolean(6, c.isAtivo());
                            stmtUpdate.setInt(7, ordem);
                            stmtUpdate.setObject(8, c.getId());
                            stmtUpdate.executeUpdate();
                        } else {
                            UUID novoId = c.getId() != null ? c.getId() : UUID.randomUUID();
                            stmtInsert.setObject(1, novoId);
                            stmtInsert.setString(2, c.getNome());
                            stmtInsert.setString(3, c.getDescricao());
                            stmtInsert.setBigDecimal(4, BigDecimal.valueOf(c.getPontosBase()));
                            stmtInsert.setString(5, c.getCategoria());
                            stmtInsert.setString(6, c.getTipoResposta());
                            stmtInsert.setBoolean(7, c.isAtivo());
                            stmtInsert.setInt(8, ordem);
                            stmtInsert.executeUpdate();
                        }
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return listarTodos();
    }

    private List<UUID> listarIdsExistentes(Connection conn) throws SQLException {
        String sql = "SELECT id FROM criterios";
        List<UUID> ids = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getObject("id", UUID.class));
            }
        }
        return ids;
    }
}
