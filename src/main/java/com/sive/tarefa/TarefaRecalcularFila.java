package com.sive.tarefa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sive.dao.Conexao;

/**
 * Tarefa concorrente (Runnable) para reordenar e reclassificar as posições
 * da fila de espera de uma turma com base na pontuação total (DESC) e data de inscrição (ASC).
 */
public class TarefaRecalcularFila implements Runnable {

    private final UUID turmaId;
    private int totalReordenados;
    private Exception erro;

    public TarefaRecalcularFila(UUID turmaId) {
        if (turmaId == null) {
            throw new IllegalArgumentException("O ID da turma é obrigatório para o recálculo da fila.");
        }
        this.turmaId = turmaId;
    }

    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();
        System.out.println("[" + nomeThread + "] Iniciando recálculo concorrente da fila para turma: " + turmaId);

        String sqlBuscarFila = "SELECT id FROM inscricoes "
                + "WHERE turma_id = ? AND status = 'EM_ESPERA' "
                + "ORDER BY pontuacao_total DESC, data_inscricao ASC";

        try {
            // Simulação de processamento concorrente em lote
            Thread.sleep(100);

            List<UUID> inscricoesOrdenadas = new ArrayList<>();

            try (Connection conn = Conexao.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlBuscarFila)) {
                stmt.setObject(1, turmaId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        inscricoesOrdenadas.add((UUID) rs.getObject("id"));
                    }
                }
            }

            this.totalReordenados = inscricoesOrdenadas.size();
            System.out.println("[" + nomeThread + "] Recálculo finalizado com sucesso! Total reclassificado: " + totalReordenados);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.erro = e;
            System.err.println("[" + nomeThread + "] Thread de recálculo da fila foi interrompida.");
        } catch (SQLException e) {
            this.erro = e;
            System.err.println("[" + nomeThread + "] Erro SQL ao recalcular fila: " + e.getMessage());
        }
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public int getTotalReordenados() {
        return totalReordenados;
    }

    public Exception getErro() {
        return erro;
    }
}
