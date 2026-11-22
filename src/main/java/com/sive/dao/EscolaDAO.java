package com.sive.dao;

import com.sive.model.Escola;
import com.sive.model.ResumoEscola;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) para a entidade Escola utilizando JDBC.
 */
public class EscolaDAO {

    /**
     * Insere uma nova escola no banco de dados.
     */
    public Escola inserir(Escola escola) throws SQLException {
        String sql = "INSERT INTO escolas (id, codigo, nome, bairro, cidade, capacidade_total, senha_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        if (escola.getId() == null) {
            escola.setId(UUID.randomUUID());
        }

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, escola.getId());
            stmt.setString(2, escola.getCodigo());
            stmt.setString(3, escola.getNome());
            stmt.setString(4, escola.getBairro());
            stmt.setString(5, escola.getCidade());
            stmt.setInt(6, escola.getCapacidadeTotal());
            stmt.setString(7, escola.getSenhaHash());

            stmt.executeUpdate();
            return escola;
        }
    }

    /**
     * Retorna a lista de todas as escolas cadastradas ordenadas por nome.
     */
    public List<Escola> listar() throws SQLException {
        List<Escola> escolas = new ArrayList<>();
        String sql = "SELECT id, codigo, nome, bairro, cidade, capacidade_total FROM escolas ORDER BY nome ASC";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                escolas.add(mapearEscolaSemSenha(rs));
            }
        }
        return escolas;
    }

    /**
     * Busca uma escola pelo seu identificador único (UUID).
     */
    public Escola buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, codigo, nome, bairro, cidade, capacidade_total FROM escolas WHERE id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEscolaSemSenha(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca uma escola pelo código identificador (ex: EMEI-0012), incluindo o hash da senha para autenticação.
     */
    public Escola buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT id, codigo, nome, bairro, cidade, capacidade_total, senha_hash FROM escolas WHERE codigo = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Escola(
                            (UUID) rs.getObject("id"),
                            rs.getString("codigo"),
                            rs.getString("nome"),
                            rs.getString("bairro"),
                            rs.getString("cidade"),
                            rs.getInt("capacidade_total"),
                            rs.getString("senha_hash"));
                }
            }
        }
        return null;
    }

    /**
     * Verifica se já existe uma escola cadastrada com o código informado.
     */
    public boolean existePorCodigo(String codigo) throws SQLException {
        String sql = "SELECT 1 FROM escolas WHERE codigo = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Retorna os indicadores resumidos da escola informada.
     */
    public ResumoEscola obterResumo(UUID escolaId) throws SQLException {
        Escola escola = buscarPorId(escolaId);
        if (escola == null) {
            return null;
        }

        String sqlEmEspera = "SELECT COUNT(*) AS total "
                + "FROM inscricoes i "
                + "INNER JOIN turmas t ON t.id = i.turma_id "
                + "WHERE t.escola_id = ? AND i.status = 'EM_ESPERA'";

        String sqlVagas = "SELECT COALESCE(SUM(vagas_disponiveis), 0) AS total "
                + "FROM turmas WHERE escola_id = ?";

        String sqlResponsaveis = "SELECT COUNT(DISTINCT ar.responsavel_id) AS total "
                + "FROM aluno_responsaveis ar "
                + "INNER JOIN inscricoes i ON i.aluno_id = ar.aluno_id "
                + "INNER JOIN turmas t ON t.id = i.turma_id "
                + "WHERE t.escola_id = ?";

        String sqlAlunos = "SELECT COUNT(DISTINCT i.aluno_id) AS total "
                + "FROM inscricoes i "
                + "INNER JOIN turmas t ON t.id = i.turma_id "
                + "WHERE t.escola_id = ?";

        try (Connection conn = Conexao.getConnection()) {
            int totalEmEspera = contarPorEscola(conn, sqlEmEspera, escolaId);
            int vagasDisponiveis = contarPorEscola(conn, sqlVagas, escolaId);
            int totalResponsaveis = contarPorEscola(conn, sqlResponsaveis, escolaId);
            int totalAlunos = contarPorEscola(conn, sqlAlunos, escolaId);

            return new ResumoEscola(
                    escola,
                    totalEmEspera,
                    vagasDisponiveis,
                    escola.getCapacidadeTotal(),
                    totalResponsaveis,
                    totalAlunos);
        }
    }

    private int contarPorEscola(Connection conn, String sql, UUID escolaId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, escolaId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    private Escola mapearEscolaSemSenha(ResultSet rs) throws SQLException {
        return new Escola(
                (UUID) rs.getObject("id"),
                rs.getString("codigo"),
                rs.getString("nome"),
                rs.getString("bairro"),
                rs.getString("cidade"),
                rs.getInt("capacidade_total"));
    }
}
