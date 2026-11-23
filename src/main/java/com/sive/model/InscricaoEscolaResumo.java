package com.sive.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO com os dados resumidos de uma inscrição na fila da escola.
 */
public class InscricaoEscolaResumo {

    private UUID id;
    private UUID alunoId;
    private String alunoNome;
    private LocalDate alunoDataNascimento;
    private UUID turmaId;
    private String etapaOuTurma;
    private String tipoVaga;
    private String turno;
    private String protocolo;
    private BigDecimal pontuacaoTotal;
    private StatusInscricao status;
    private LocalDateTime dataInscricao;
    private LocalDateTime dataChamada;
    private Integer posicao;

    public InscricaoEscolaResumo() {
    }

    public InscricaoEscolaResumo(
            UUID id,
            UUID alunoId,
            String alunoNome,
            LocalDate alunoDataNascimento,
            UUID turmaId,
            String etapaOuTurma,
            String tipoVaga,
            String turno,
            String protocolo,
            BigDecimal pontuacaoTotal,
            StatusInscricao status,
            LocalDateTime dataInscricao,
            LocalDateTime dataChamada,
            Integer posicao) {
        this.id = id;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.alunoDataNascimento = alunoDataNascimento;
        this.turmaId = turmaId;
        this.etapaOuTurma = etapaOuTurma;
        this.tipoVaga = tipoVaga;
        this.turno = turno;
        this.protocolo = protocolo;
        this.pontuacaoTotal = pontuacaoTotal;
        this.status = status;
        this.dataInscricao = dataInscricao;
        this.dataChamada = dataChamada;
        this.posicao = posicao;
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

    public String getAlunoNome() {
        return alunoNome;
    }

    public void setAlunoNome(String alunoNome) {
        this.alunoNome = alunoNome;
    }

    public LocalDate getAlunoDataNascimento() {
        return alunoDataNascimento;
    }

    public void setAlunoDataNascimento(LocalDate alunoDataNascimento) {
        this.alunoDataNascimento = alunoDataNascimento;
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(UUID turmaId) {
        this.turmaId = turmaId;
    }

    public String getEtapaOuTurma() {
        return etapaOuTurma;
    }

    public void setEtapaOuTurma(String etapaOuTurma) {
        this.etapaOuTurma = etapaOuTurma;
    }

    public String getTipoVaga() {
        return tipoVaga;
    }

    public void setTipoVaga(String tipoVaga) {
        this.tipoVaga = tipoVaga;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public BigDecimal getPontuacaoTotal() {
        return pontuacaoTotal;
    }

    public void setPontuacaoTotal(BigDecimal pontuacaoTotal) {
        this.pontuacaoTotal = pontuacaoTotal;
    }

    public StatusInscricao getStatus() {
        return status;
    }

    public void setStatus(StatusInscricao status) {
        this.status = status;
    }

    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(LocalDateTime dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public LocalDateTime getDataChamada() {
        return dataChamada;
    }

    public void setDataChamada(LocalDateTime dataChamada) {
        this.dataChamada = dataChamada;
    }

    public Integer getPosicao() {
        return posicao;
    }

    public void setPosicao(Integer posicao) {
        this.posicao = posicao;
    }
}
