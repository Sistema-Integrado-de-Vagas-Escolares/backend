package com.sive.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoricoVaga {

    private UUID id;
    private UUID escolaId;
    private UUID turmaId;
    private String serieAno;
    private String turmaNome;
    private Turno turno;
    private int vagasAnteriores;
    private int vagasNovas;
    private String motivo;
    private LocalDateTime createdAt;

    public HistoricoVaga() {
    }

    public HistoricoVaga(
            UUID id,
            UUID escolaId,
            UUID turmaId,
            String serieAno,
            String turmaNome,
            Turno turno,
            int vagasAnteriores,
            int vagasNovas,
            String motivo,
            LocalDateTime createdAt) {
        this.id = id;
        this.escolaId = escolaId;
        this.turmaId = turmaId;
        this.serieAno = serieAno;
        this.turmaNome = turmaNome;
        this.turno = turno;
        this.vagasAnteriores = vagasAnteriores;
        this.vagasNovas = vagasNovas;
        this.motivo = motivo;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(UUID escolaId) {
        this.escolaId = escolaId;
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(UUID turmaId) {
        this.turmaId = turmaId;
    }

    public String getSerieAno() {
        return serieAno;
    }

    public void setSerieAno(String serieAno) {
        this.serieAno = serieAno;
    }

    public String getTurmaNome() {
        return turmaNome;
    }

    public void setTurmaNome(String turmaNome) {
        this.turmaNome = turmaNome;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public int getVagasAnteriores() {
        return vagasAnteriores;
    }

    public void setVagasAnteriores(int vagasAnteriores) {
        this.vagasAnteriores = vagasAnteriores;
    }

    public int getVagasNovas() {
        return vagasNovas;
    }

    public void setVagasNovas(int vagasNovas) {
        this.vagasNovas = vagasNovas;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
