package com.sive.model;

/**
 * DTO com os indicadores resumidos e dados cadastrais da escola autenticada.
 */
public class ResumoEscola {

    private Escola escola;
    private int totalEmEspera;
    private int vagasDisponiveis;
    private int capacidadeTotal;
    private int totalResponsaveis;
    private int totalAlunos;
    private int totalMatriculados;

    public ResumoEscola() {
    }

    public ResumoEscola(
            Escola escola,
            int totalEmEspera,
            int vagasDisponiveis,
            int capacidadeTotal,
            int totalResponsaveis,
            int totalAlunos,
            int totalMatriculados) {
        this.escola = escola;
        this.totalEmEspera = totalEmEspera;
        this.vagasDisponiveis = vagasDisponiveis;
        this.capacidadeTotal = capacidadeTotal;
        this.totalResponsaveis = totalResponsaveis;
        this.totalAlunos = totalAlunos;
        this.totalMatriculados = totalMatriculados;
    }

    public Escola getEscola() {
        return escola;
    }

    public void setEscola(Escola escola) {
        this.escola = escola;
    }

    public int getTotalEmEspera() {
        return totalEmEspera;
    }

    public void setTotalEmEspera(int totalEmEspera) {
        this.totalEmEspera = totalEmEspera;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public int getCapacidadeTotal() {
        return capacidadeTotal;
    }

    public void setCapacidadeTotal(int capacidadeTotal) {
        this.capacidadeTotal = capacidadeTotal;
    }

    public int getTotalResponsaveis() {
        return totalResponsaveis;
    }

    public void setTotalResponsaveis(int totalResponsaveis) {
        this.totalResponsaveis = totalResponsaveis;
    }

    public int getTotalAlunos() {
        return totalAlunos;
    }

    public void setTotalAlunos(int totalAlunos) {
        this.totalAlunos = totalAlunos;
    }

    public int getTotalMatriculados() {
        return totalMatriculados;
    }

    public void setTotalMatriculados(int totalMatriculados) {
        this.totalMatriculados = totalMatriculados;
    }

    @Override
    public String toString() {
        return "ResumoEscola{"
                + "escola=" + escola
                + ", totalEmEspera=" + totalEmEspera
                + ", vagasDisponiveis=" + vagasDisponiveis
                + ", capacidadeTotal=" + capacidadeTotal
                + ", totalResponsaveis=" + totalResponsaveis
                + ", totalAlunos=" + totalAlunos
                + '}';
    }
}
