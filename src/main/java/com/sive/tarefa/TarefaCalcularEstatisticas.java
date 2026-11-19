package com.sive.tarefa;

import java.sql.SQLException;

import com.sive.dao.EstatisticasDAO;
import com.sive.model.EstatisticasRede;

/**
 * Tarefa concorrente para calcular e agregar metricas gerais da rede escolar.
 */
public class TarefaCalcularEstatisticas implements Runnable {

    private final EstatisticasDAO estatisticasDAO;
    private EstatisticasRede resultado;
    private Exception erro;

    public TarefaCalcularEstatisticas() {
        this.estatisticasDAO = new EstatisticasDAO();
    }

    public TarefaCalcularEstatisticas(EstatisticasDAO estatisticasDAO) {
        this.estatisticasDAO = estatisticasDAO;
    }

    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();
        System.out.println("[" + nomeThread + "] Iniciando calculo concorrente de estatisticas da rede...");

        try {
            Thread.sleep(100);

            this.resultado = estatisticasDAO.calcularEstatisticas();

            System.out.println("[" + nomeThread + "] Calculo finalizado com sucesso: " + resultado);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.erro = e;
            System.err.println("[" + nomeThread + "] Thread de estatisticas foi interrompida.");
        } catch (SQLException e) {
            this.erro = e;
            System.err.println(
                    "[" + nomeThread + "] Erro ao consultar banco de dados durante estatisticas: " + e.getMessage());
        }
    }

    public EstatisticasRede getResultado() {
        return resultado;
    }

    public Exception getErro() {
        return erro;
    }
}
