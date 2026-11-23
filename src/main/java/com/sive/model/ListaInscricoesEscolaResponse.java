package com.sive.model;

import java.util.List;

/**
 * Resposta agregada com os itens filtrados da fila da escola e os totais por status.
 */
public class ListaInscricoesEscolaResponse {

    private List<InscricaoEscolaResumo> itens;
    private int totalEmEspera;
    private int totalAguardandoMatricula;
    private int totalMatriculados;

    public ListaInscricoesEscolaResponse() {
    }

    public ListaInscricoesEscolaResponse(
            List<InscricaoEscolaResumo> itens,
            int totalEmEspera,
            int totalAguardandoMatricula,
            int totalMatriculados) {
        this.itens = itens;
        this.totalEmEspera = totalEmEspera;
        this.totalAguardandoMatricula = totalAguardandoMatricula;
        this.totalMatriculados = totalMatriculados;
    }

    public List<InscricaoEscolaResumo> getItens() {
        return itens;
    }

    public void setItens(List<InscricaoEscolaResumo> itens) {
        this.itens = itens;
    }

    public int getTotalEmEspera() {
        return totalEmEspera;
    }

    public void setTotalEmEspera(int totalEmEspera) {
        this.totalEmEspera = totalEmEspera;
    }

    public int getTotalAguardandoMatricula() {
        return totalAguardandoMatricula;
    }

    public void setTotalAguardandoMatricula(int totalAguardandoMatricula) {
        this.totalAguardandoMatricula = totalAguardandoMatricula;
    }

    public int getTotalMatriculados() {
        return totalMatriculados;
    }

    public void setTotalMatriculados(int totalMatriculados) {
        this.totalMatriculados = totalMatriculados;
    }
}
