package com.sive.dao;

import com.sive.model.Inscricao;
import com.sive.model.InscricaoCriterio;
import com.sive.model.StatusInscricao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InscricaoDAO {

    public Inscricao inserir(Inscricao inscricao) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                Inscricao resultado = inserir(inscricao, conn);
                if (inscricao.getCriterios() != null && !inscricao.getCriterios().isEmpty()) {
                    inserirCriterios(inscricao.getId(), inscricao.getCriterios(), conn);
                }
                conn.commit();
                return resultado;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public Inscricao inserir(Inscricao inscricao, Connection conn) throws SQLException {
        String sql = "INSERT INTO inscricoes (id, aluno_id, turma_id, protocolo, data_inscricao, pontuacao_total, status, data_chamada) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        if (inscricao.getId() == null) {
            inscricao.setId(UUID.randomUUID());
        }
        if (inscricao.getDataInscricao() == null) {
            inscricao.setDataInscricao(LocalDateTime.now());
        }
        if (inscricao.getStatus() == null) {
            inscricao.setStatus(StatusInscricao.EM_ESPERA);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricao.getId());
            stmt.setObject(2, inscricao.getAlunoId());
            stmt.setObject(3, inscricao.getTurmaId());
            stmt.setString(4, inscricao.getProtocolo());
            stmt.setTimestamp(5, Timestamp.valueOf(inscricao.getDataInscricao()));
            stmt.setBigDecimal(6, BigDecimal.valueOf(inscricao.getPontuacaoTotal()));
            stmt.setString(7, inscricao.getStatus().name());
            stmt.setTimestamp(8, inscricao.getDataChamada() != null ? Timestamp.valueOf(inscricao.getDataChamada()) : null);

            stmt.executeUpdate();
            return inscricao;
        }
    }

    public Inscricao buscarPorId(UUID id) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return buscarPorId(id, conn);
        }
    }

    public Inscricao buscarPorId(UUID id, Connection conn) throws SQLException {
        String sql = "SELECT id, aluno_id, turma_id, protocolo, data_inscricao, pontuacao_total, status, data_chamada, created_at "
                + "FROM inscricoes WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Inscricao inscricao = mapearResultSet(rs);
                    inscricao.setCriterios(buscarCriteriosPorInscricao(inscricao.getId(), conn));
                    return inscricao;
                }
            }
        }
        return null;
    }

    public Inscricao buscarPorProtocolo(String protocolo) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return buscarPorProtocolo(protocolo, conn);
        }
    }

    public Inscricao buscarPorProtocolo(String protocolo, Connection conn) throws SQLException {
        String sql = "SELECT id, aluno_id, turma_id, protocolo, data_inscricao, pontuacao_total, status, data_chamada, created_at "
                + "FROM inscricoes WHERE protocolo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, protocolo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Inscricao inscricao = mapearResultSet(rs);
                    inscricao.setCriterios(buscarCriteriosPorInscricao(inscricao.getId(), conn));
                    return inscricao;
                }
            }
        }
        return null;
    }

    public Inscricao buscarPorAlunoETurma(UUID alunoId, UUID turmaId) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return buscarPorAlunoETurma(alunoId, turmaId, conn);
        }
    }

    public Inscricao buscarPorAlunoETurma(UUID alunoId, UUID turmaId, Connection conn) throws SQLException {
        String sql = "SELECT id, aluno_id, turma_id, protocolo, data_inscricao, pontuacao_total, status, data_chamada, created_at "
                + "FROM inscricoes WHERE aluno_id = ? AND turma_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, alunoId);
            stmt.setObject(2, turmaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Inscricao inscricao = mapearResultSet(rs);
                    inscricao.setCriterios(buscarCriteriosPorInscricao(inscricao.getId(), conn));
                    return inscricao;
                }
            }
        }
        return null;
    }

    public List<Inscricao> listarPorTurma(UUID turmaId) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return listarPorTurma(turmaId, conn);
        }
    }

    public List<Inscricao> listarPorTurma(UUID turmaId, Connection conn) throws SQLException {
        List<Inscricao> lista = new ArrayList<>();
        String sql = "SELECT id, aluno_id, turma_id, protocolo, data_inscricao, pontuacao_total, status, data_chamada, created_at "
                + "FROM inscricoes WHERE turma_id = ? "
                + "ORDER BY pontuacao_total DESC, data_inscricao ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, turmaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        }
        return lista;
    }

    public List<Inscricao> listarPorTurmaEStatus(UUID turmaId, StatusInscricao status) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return listarPorTurmaEStatus(turmaId, status, conn);
        }
    }

    public List<Inscricao> listarPorTurmaEStatus(UUID turmaId, StatusInscricao status, Connection conn) throws SQLException {
        List<Inscricao> lista = new ArrayList<>();
        String sql = "SELECT id, aluno_id, turma_id, protocolo, data_inscricao, pontuacao_total, status, data_chamada, created_at "
                + "FROM inscricoes WHERE turma_id = ? AND status = ? "
                + "ORDER BY pontuacao_total DESC, data_inscricao ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, turmaId);
            stmt.setString(2, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        }
        return lista;
    }

    public void atualizarStatus(UUID id, StatusInscricao novoStatus) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            atualizarStatus(id, novoStatus, conn);
        }
    }

    public void atualizarStatus(UUID id, StatusInscricao novoStatus, Connection conn) throws SQLException {
        if (novoStatus == StatusInscricao.CHAMADO) {
            atualizarStatus(id, novoStatus, LocalDateTime.now(), conn);
        } else {
            String sql = "UPDATE inscricoes SET status = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, novoStatus.name());
                stmt.setObject(2, id);
                stmt.executeUpdate();
            }
        }
    }

    public void atualizarStatus(UUID id, StatusInscricao novoStatus, LocalDateTime dataChamada, Connection conn) throws SQLException {
        String sql = "UPDATE inscricoes SET status = ?, data_chamada = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus.name());
            stmt.setTimestamp(2, dataChamada != null ? Timestamp.valueOf(dataChamada) : null);
            stmt.setObject(3, id);
            stmt.executeUpdate();
        }
    }

    public void atualizarPontuacao(UUID id, double pontuacaoTotal) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            atualizarPontuacao(id, pontuacaoTotal, conn);
        }
    }

    public void atualizarPontuacao(UUID id, double pontuacaoTotal, Connection conn) throws SQLException {
        String sql = "UPDATE inscricoes SET pontuacao_total = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, BigDecimal.valueOf(pontuacaoTotal));
            stmt.setObject(2, id);
            stmt.executeUpdate();
        }
    }

    public void inserirCriterios(UUID inscricaoId, List<InscricaoCriterio> criterios) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                inserirCriterios(inscricaoId, criterios, conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public void inserirCriterios(UUID inscricaoId, List<InscricaoCriterio> criterios, Connection conn) throws SQLException {
        if (criterios == null || criterios.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO inscricao_criterios (id, inscricao_id, criterio_id, pontos_obtidos) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (InscricaoCriterio c : criterios) {
                if (c.getId() == null) {
                    c.setId(UUID.randomUUID());
                }
                stmt.setObject(1, c.getId());
                stmt.setObject(2, inscricaoId != null ? inscricaoId : c.getInscricaoId());
                stmt.setObject(3, c.getCriterioId());
                stmt.setBigDecimal(4, BigDecimal.valueOf(c.getPontosObtidos()));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<InscricaoCriterio> buscarCriteriosPorInscricao(UUID inscricaoId) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            return buscarCriteriosPorInscricao(inscricaoId, conn);
        }
    }

    public List<InscricaoCriterio> buscarCriteriosPorInscricao(UUID inscricaoId, Connection conn) throws SQLException {
        List<InscricaoCriterio> lista = new ArrayList<>();
        String sql = "SELECT id, inscricao_id, criterio_id, pontos_obtidos FROM inscricao_criterios WHERE inscricao_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal pontosBd = rs.getBigDecimal("pontos_obtidos");
                    double pontos = pontosBd != null ? pontosBd.doubleValue() : 0.0;
                    InscricaoCriterio ic = new InscricaoCriterio(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("inscricao_id"),
                            (UUID) rs.getObject("criterio_id"),
                            pontos
                    );
                    lista.add(ic);
                }
            }
        }
        return lista;
    }

    private Inscricao mapearResultSet(ResultSet rs) throws SQLException {
        Timestamp dataInscricaoTs = rs.getTimestamp("data_inscricao");
        LocalDateTime dataInscricao = dataInscricaoTs != null ? dataInscricaoTs.toLocalDateTime() : null;

        Timestamp dataChamadaTs = rs.getTimestamp("data_chamada");
        LocalDateTime dataChamada = dataChamadaTs != null ? dataChamadaTs.toLocalDateTime() : null;

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;

        BigDecimal pontuacaoBigDecimal = rs.getBigDecimal("pontuacao_total");
        double pontuacaoTotal = pontuacaoBigDecimal != null ? pontuacaoBigDecimal.doubleValue() : 0.0;

        String statusStr = rs.getString("status");
        StatusInscricao status = StatusInscricao.fromString(statusStr);

        return new Inscricao(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("aluno_id"),
                (UUID) rs.getObject("turma_id"),
                rs.getString("protocolo"),
                dataInscricao,
                pontuacaoTotal,
                status,
                dataChamada,
                createdAt
        );
    }
}
