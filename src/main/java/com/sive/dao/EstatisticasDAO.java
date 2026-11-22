package com.sive.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.sive.model.EstatisticasRede;

/**
 * Data Access Object (DAO) para agregações e métricas gerais do sistema.
 */
public class EstatisticasDAO {

    /**
     * Consulta o banco de dados e calcula as estatísticas agregadas da rede.
     */
    public EstatisticasRede calcularEstatisticas() throws SQLException {
        EstatisticasRede estatisticas = new EstatisticasRede();
        estatisticas.setDataCalculo(LocalDateTime.now());

        String sqlEscolas = "SELECT COUNT(*) AS total FROM escolas";
        String sqlTurmas = "SELECT COUNT(*) AS total_turmas, "
                + "COALESCE(SUM(vagas_totais), 0) AS total_vagas, "
                + "COALESCE(SUM(vagas_disponiveis), 0) AS vagas_disponiveis "
                + "FROM turmas";
        String sqlInscricoes = "SELECT "
                + "COUNT(*) AS total_inscricoes, "
                + "COUNT(*) FILTER (WHERE status = 'EM_ESPERA') AS total_espera, "
                + "COUNT(*) FILTER (WHERE status = 'CHAMADO') AS total_chamados, "
                + "COUNT(*) FILTER (WHERE status = 'MATRICULADO') AS total_matriculados "
                + "FROM inscricoes";

        try (Connection conn = Conexao.getConnection()) {
            // 1. Escolas
            try (PreparedStatement stmt = conn.prepareStatement(sqlEscolas);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    estatisticas.setTotalEscolas(rs.getInt("total"));
                }
            }

            // 2. Turmas e Vagas
            try (PreparedStatement stmt = conn.prepareStatement(sqlTurmas);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalTurmas = rs.getInt("total_turmas");
                    int totalVagas = rs.getInt("total_vagas");
                    int vagasDisponiveis = rs.getInt("vagas_disponiveis");
                    int vagasOcupadas = Math.max(0, totalVagas - vagasDisponiveis);

                    estatisticas.setTotalTurmas(totalTurmas);
                    estatisticas.setTotalVagas(totalVagas);
                    estatisticas.setTotalVagasDisponiveis(vagasDisponiveis);
                    estatisticas.setTotalVagasOcupadas(vagasOcupadas);

                    if (totalVagas > 0) {
                        double taxa = ((double) vagasOcupadas / totalVagas) * 100.0;
                        estatisticas.setTaxaOcupacaoGeral(taxa);
                    } else {
                        estatisticas.setTaxaOcupacaoGeral(0.0);
                    }
                }
            }

            // 3. Inscricoes e Filas
            try (PreparedStatement stmt = conn.prepareStatement(sqlInscricoes);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    estatisticas.setTotalInscricoes(rs.getInt("total_inscricoes"));
                    estatisticas.setTotalEmEspera(rs.getInt("total_espera"));
                    estatisticas.setTotalChamados(rs.getInt("total_chamados"));
                    estatisticas.setTotalMatriculados(rs.getInt("total_matriculados"));
                }
            }
        }

        return estatisticas;
    }
}
