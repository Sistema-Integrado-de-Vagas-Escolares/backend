package com.sive.model;

import java.util.UUID;

public class InscricaoCriterio {

    private UUID id;
    private UUID inscricaoId;
    private UUID criterioId;
    private double pontosObtidos;

    public InscricaoCriterio() {
    }

    public InscricaoCriterio(UUID inscricaoId, UUID criterioId, double pontosObtidos) {
        this.inscricaoId = inscricaoId;
        this.criterioId = criterioId;
        this.pontosObtidos = pontosObtidos;
    }

    public InscricaoCriterio(UUID id, UUID inscricaoId, UUID criterioId, double pontosObtidos) {
        this.id = id;
        this.inscricaoId = inscricaoId;
        this.criterioId = criterioId;
        this.pontosObtidos = pontosObtidos;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInscricaoId() {
        return inscricaoId;
    }

    public void setInscricaoId(UUID inscricaoId) {
        this.inscricaoId = inscricaoId;
    }

    public UUID getCriterioId() {
        return criterioId;
    }

    public void setCriterioId(UUID criterioId) {
        this.criterioId = criterioId;
    }

    public double getPontosObtidos() {
        return pontosObtidos;
    }

    public void setPontosObtidos(double pontosObtidos) {
        this.pontosObtidos = pontosObtidos;
    }

    @Override
    public String toString() {
        return "InscricaoCriterio{" +
                "id=" + id +
                ", inscricaoId=" + inscricaoId +
                ", criterioId=" + criterioId +
                ", pontosObtidos=" + pontosObtidos +
                '}';
    }
}
