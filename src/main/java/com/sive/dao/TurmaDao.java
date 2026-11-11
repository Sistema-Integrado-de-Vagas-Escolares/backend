package com.sive.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sive.model.Escola;
import com.sive.model.Turma;

/**
 * Data Access Object (DAO) para a entidade Escola utilizando JDBC.
 */
public class TurmaDao {

    /**
     * Insere uma nova turma no banco de dados.
     */
    public Turma inserir(Turma turma) throws SQLException {
        String sql = "INSERT INTO turmas (id, escola_id, serie_ano, turno, vagas_totais, vagas_disponiveis) VALUES (?, ?, ?, ?, ?, ?)";

        if (turma.getId() == null) {
            turma.setId(UUID.randomUUID());
        }

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, turma.getId());
            stmt.setObject(2, turma.getEscolaId());
            stmt.setString(3, turma.getSerieAno());
            stmt.setString(4, turma.getTurno().toString());
            stmt.setInt(5, turma.getVagasTotais());
            stmt.setInt(6, turma.getVagasDisponiveis());

            stmt.executeUpdate();
            return turma;
        }
    }

    /**
     * Retorna a lista de todas as turmas por escola cadastradas ordenadas por nome.
     */
    public List<Turma> listarPorEscola(UUID escolaId) throws SQLException {
        List<Turma> turmas = new ArrayList<>();
        String sql = "SELECT id, escola_id, serie_ano, turno, vagas_totais, vagas_disponiveis, created_at FROM turmas WHERE escola_id = ? ORDER BY nome ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Turma escola = new Turma(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("escola_id"),
                        rs.getString("serie_ano"),
                        (Turno) rs.getObject("turno"),
                        rs.getInt("vagas_totais"),
                        rs.getInt("vagas_disponiveis"),
                        rs.getTimestamp("created_at").toLocalDateTime());
                turmas.add(escola);
            }
        }
        return turmas;
    }
}
