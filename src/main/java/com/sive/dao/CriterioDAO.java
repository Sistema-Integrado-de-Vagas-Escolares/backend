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

@Repository
public class CriterioDAO {

    public List<Criterio> listarTodos() throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return listarTodos(conn);
        }
    }

    public List<Criterio> listarTodos(Connection conn) throws SQLException {
        String sql = "SELECT id, codigo, nome, descricao, pontos_base, categoria, tipo_resposta, ativo, ordem, created_at "
                + "FROM criterios "
                + "ORDER BY ordem ASC, created_at ASC";

        List<Criterio> lista = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                Criterio c = new Criterio(
                        rs.getObject("id", UUID.class),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getBigDecimal("pontos_base").doubleValue(),
                        rs.getString("categoria"),
                        rs.getString("tipo_resposta"),
                        rs.getBoolean("ativo"),
                        rs.getInt("ordem"),
                        ts != null ? ts.toLocalDateTime() : null);
                c.setCodigo(rs.getString("codigo"));
                lista.add(c);
            }
        }
        return lista;
    }


public List<Criterio> substituirTodos(List<Criterio> criterios) throws SQLException {
    try (Connection conn = Conexao.getConnection()) {
        conn.setAutoCommit(false);
        try {
            List<Criterio> resultado = substituirTodos(conn, criterios);
            conn.commit();
            return resultado;
        } catch (SQLException | RuntimeException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}

    public List<Criterio> substituirTodos(
            Connection conn,
            List<Criterio> criterios) throws SQLException {

        String sqlDesativarRemovidos = "UPDATE criterios SET ativo = FALSE WHERE id = ?";
        String sqlUpdate = "UPDATE criterios SET codigo = ?, nome = ?, descricao = ?, pontos_base = ?, categoria = ?, "
                + "tipo_resposta = ?, ativo = ?, ordem = ? WHERE id = ?";
        String sqlInsert = "INSERT INTO criterios (id, codigo, nome, descricao, pontos_base, categoria, tipo_resposta, ativo, ordem) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<UUID> existentes = listarIdsExistentes(conn);
        List<UUID> mantidos = new ArrayList<>();

        for (Criterio criterio : criterios) {
            if (criterio.getId() != null) {
                mantidos.add(criterio.getId());
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement(sqlDesativarRemovidos)) {
            for (UUID idExistente : existentes) {
                if (!mantidos.contains(idExistente)) {
                    stmt.setObject(1, idExistente);
                    stmt.executeUpdate();
                }
            }
        }

        try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
                PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {

            for (int i = 0; i < criterios.size(); i++) {
                Criterio c = criterios.get(i);
                int ordem = i + 1;

                if (c.getId() != null && existentes.contains(c.getId())) {
                    stmtUpdate.setString(1, c.getCodigo());
                    stmtUpdate.setString(2, c.getNome());
                    stmtUpdate.setString(3, c.getDescricao());
                    stmtUpdate.setBigDecimal(4, BigDecimal.valueOf(c.getPontosBase()));
                    stmtUpdate.setString(5, c.getCategoria());
                    stmtUpdate.setString(6, c.getTipoResposta());
                    stmtUpdate.setBoolean(7, c.isAtivo());
                    stmtUpdate.setInt(8, ordem);
                    stmtUpdate.setObject(9, c.getId());
                    stmtUpdate.executeUpdate();
                } else {
                    UUID novoId = c.getId() != null ? c.getId() : UUID.randomUUID();
                    stmtInsert.setObject(1, novoId);
                    stmtInsert.setString(2, c.getCodigo());
                    stmtInsert.setString(3, c.getNome());
                    stmtInsert.setString(4, c.getDescricao());
                    stmtInsert.setBigDecimal(5, BigDecimal.valueOf(c.getPontosBase()));
                    stmtInsert.setString(6, c.getCategoria());
                    stmtInsert.setString(7, c.getTipoResposta());
                    stmtInsert.setBoolean(8, c.isAtivo());
                    stmtInsert.setInt(9, ordem);
                    stmtInsert.executeUpdate();
                }
            }
        }

        return listarTodos(conn);
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
