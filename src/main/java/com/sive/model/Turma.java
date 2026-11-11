package com.sive.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Turma {
    private UUID id;
    private UUID escolaId;
    private String serieAno;
    private Turno turno;
    private int vagasTotais;
    private int vagasDisponiveis;
    private LocalDateTime dataCriacao;

    public Turma() {
    }

    public Turma(UUID escolaId, String serieAno, Turno turno, int vagasTotais, int vagasDisponiveis) {
        this.escolaId = escolaId;
        this.serieAno = serieAno;
        this.turno = turno;
        this.vagasTotais = vagasTotais;
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public Turma(UUID id, UUID escolaId, String serieAno, Turno turno, int vagasTotais, int vagasDisponiveis,
            LocalDateTime dataCriacao) {
        this.id = id;
        this.escolaId = escolaId;
        this.serieAno = serieAno;
        this.turno = turno;
        this.vagasTotais = vagasTotais;
        this.vagasDisponiveis = vagasDisponiveis;
        this.dataCriacao = dataCriacao;
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

    public String getSerieAno() {
        return serieAno;
    }

    public void setSerieAno(String serieAno) {
        this.serieAno = serieAno;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public int getVagasTotais() {
        return vagasTotais;
    }

    public void setVagasTotais(int vagasTotais) {
        this.vagasTotais = vagasTotais;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public String toString() {
        return "Turma{" +
                "id=" + id +
                ", escolaId=" + escolaId +
                ", serieAno='" + serieAno + '\'' +
                ", turno=" + turno +
                ", vagasTotais=" + vagasTotais +
                ", vagasDisponiveis=" + vagasDisponiveis +
                ", dataCriacao=" + dataCriacao +
                '}';
    }

}
