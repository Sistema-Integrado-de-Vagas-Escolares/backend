package com.sive.model;

import java.time.LocalDateTime;

public class EstatisticasRede {

    private int totalEscolas;
    private int totalTurmas;
    private int totalVagas;
    private int totalVagasDisponiveis;
    private int totalVagasOcupadas;
    private int totalInscricoes;
    private int totalEmEspera;
    private int totalChamados;
    private int totalMatriculados;
    private double taxaOcupacaoGeral;
    private LocalDateTime dataCalculo;

    public EstatisticasRede() {
    }

    public int getTotalEscolas() {
        return totalEscolas;
    }

    public void setTotalEscolas(int totalEscolas) {
        this.totalEscolas = totalEscolas;
    }

    public int getTotalTurmas() {
        return totalTurmas;
    }

    public void setTotalTurmas(int totalTurmas) {
        this.totalTurmas = totalTurmas;
    }

    public int getTotalVagas() {
        return totalVagas;
    }

    public void setTotalVagas(int totalVagas) {
        this.totalVagas = totalVagas;
    }

    public int getTotalVagasDisponiveis() {
        return totalVagasDisponiveis;
    }

    public void setTotalVagasDisponiveis(int totalVagasDisponiveis) {
        this.totalVagasDisponiveis = totalVagasDisponiveis;
    }

    public int getTotalVagasOcupadas() {
        return totalVagasOcupadas;
    }

    public void setTotalVagasOcupadas(int totalVagasOcupadas) {
        this.totalVagasOcupadas = totalVagasOcupadas;
    }

    public int getTotalInscricoes() {
        return totalInscricoes;
    }

    public void setTotalInscricoes(int totalInscricoes) {
        this.totalInscricoes = totalInscricoes;
    }

    public int getTotalEmEspera() {
        return totalEmEspera;
    }

    public void setTotalEmEspera(int totalEmEspera) {
        this.totalEmEspera = totalEmEspera;
    }

    public int getTotalChamados() {
        return totalChamados;
    }

    public void setTotalChamados(int totalChamados) {
        this.totalChamados = totalChamados;
    }

    public int getTotalMatriculados() {
        return totalMatriculados;
    }

    public void setTotalMatriculados(int totalMatriculados) {
        this.totalMatriculados = totalMatriculados;
    }

    public double getTaxaOcupacaoGeral() {
        return taxaOcupacaoGeral;
    }

    public void setTaxaOcupacaoGeral(double taxaOcupacaoGeral) {
        this.taxaOcupacaoGeral = taxaOcupacaoGeral;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }

    public void setDataCalculo(LocalDateTime dataCalculo) {
        this.dataCalculo = dataCalculo;
    }

    @Override
    public String toString() {
        return "EstatisticasRede{" +
                "totalEscolas=" + totalEscolas +
                ", totalTurmas=" + totalTurmas +
                ", totalVagas=" + totalVagas +
                ", totalVagasDisponiveis=" + totalVagasDisponiveis +
                ", totalVagasOcupadas=" + totalVagasOcupadas +
                ", totalInscricoes=" + totalInscricoes +
                ", totalEmEspera=" + totalEmEspera +
                ", totalChamados=" + totalChamados +
                ", totalMatriculados=" + totalMatriculados +
                ", taxaOcupacaoGeral=" + String.format("%.2f%%", taxaOcupacaoGeral) +
                ", dataCalculo=" + dataCalculo +
                '}';
    }
}
