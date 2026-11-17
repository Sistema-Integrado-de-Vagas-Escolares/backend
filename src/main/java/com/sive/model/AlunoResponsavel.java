package com.sive.model;

import java.util.UUID;

public class AlunoResponsavel {

    private UUID id;
    private UUID alunoId;
    private UUID responsavelId;
    private Parentesco parentesco;
    private boolean contatoPrincipal;

    public AlunoResponsavel() {
    }

    public AlunoResponsavel(UUID alunoId, UUID responsavelId, Parentesco parentesco,
            boolean contatoPrincipal) {
        this.alunoId = alunoId;
        this.responsavelId = responsavelId;
        this.parentesco = parentesco;
        this.contatoPrincipal = contatoPrincipal;
    }

    public AlunoResponsavel(UUID id, UUID alunoId, UUID responsavelId, Parentesco parentesco,
            boolean contatoPrincipal) {
        this.id = id;
        this.alunoId = alunoId;
        this.responsavelId = responsavelId;
        this.parentesco = parentesco;
        this.contatoPrincipal = contatoPrincipal;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(UUID alunoId) {
        this.alunoId = alunoId;
    }

    public UUID getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(UUID responsavelId) {
        this.responsavelId = responsavelId;
    }

    public Parentesco getParentesco() {
        return parentesco;
    }

    public void setParentesco(Parentesco parentesco) {
        this.parentesco = parentesco;
    }

    public boolean isContatoPrincipal() {
        return contatoPrincipal;
    }

    public void setContatoPrincipal(boolean contatoPrincipal) {
        this.contatoPrincipal = contatoPrincipal;
    }

    @Override
    public String toString() {
        return "AlunoResponsavel{" +
                "id=" + id +
                ", alunoId=" + alunoId +
                ", responsavelId=" + responsavelId +
                ", parentesco=" + parentesco +
                ", contatoPrincipal=" + contatoPrincipal +
                '}';
    }
}
