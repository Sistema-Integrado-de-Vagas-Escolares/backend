package com.sive.dao;

import com.sive.model.AlunoDetalhe;
import com.sive.model.InscricaoCriterio;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class InscricaoCriterioDAO {

    public void substituirPorInscricao(
            Connection conn,
            UUID inscricaoId,
            List<InscricaoCriterio> itens) throws SQLException {
        String sqlDelete = "DELETE FROM inscricao_criterios WHERE inscricao_id = ?";
        try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
            stmtDelete.setObject(1, inscricaoId);
            stmtDelete.executeUpdate();
        }

        if (itens == null || itens.isEmpty()) {
            return;
        }

        String sqlInsert = "INSERT INTO inscricao_criterios (id, inscricao_id, criterio_id, pontos_obtidos) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
            for (InscricaoCriterio item : itens) {
                UUID id = item.getId() != null ? item.getId() : UUID.randomUUID();
                stmtInsert.setObject(1, id);
                stmtInsert.setObject(2, inscricaoId);
                stmtInsert.setObject(3, item.getCriterioId());
                stmtInsert.setBigDecimal(4, BigDecimal.valueOf(item.getPontosObtidos()));
                stmtInsert.addBatch();
            }
            stmtInsert.executeBatch();
        }
    }

    public List<AlunoDetalhe.CriterioPontuadoDetalhe> listarDetalhesPorInscricao(
            Connection conn,
            UUID inscricaoId) throws SQLException {
        String sql = "SELECT c.id AS criterio_id, c.codigo, c.nome, c.categoria, c.pontos_base, ic.pontos_obtidos "
                + "FROM inscricao_criterios ic "
                + "INNER JOIN criterios c ON c.id = ic.criterio_id "
                + "WHERE ic.inscricao_id = ? "
                + "ORDER BY c.ordem ASC, c.nome ASC";

        List<AlunoDetalhe.CriterioPontuadoDetalhe> detalhes = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detalhes.add(new AlunoDetalhe.CriterioPontuadoDetalhe(
                            (UUID) rs.getObject("criterio_id"),
                            rs.getString("codigo"),
                            rs.getString("nome"),
                            rs.getString("categoria"),
                            rs.getBigDecimal("pontos_base"),
                            rs.getBigDecimal("pontos_obtidos")));
                }
            }
        }
        return detalhes;
    }
}
