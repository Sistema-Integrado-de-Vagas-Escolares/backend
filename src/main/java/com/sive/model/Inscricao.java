package com.sive.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sive.strategy.CriterioPontuacaoStrategy;

public class Inscricao {

    private UUID id;
    private UUID alunoId;
    private UUID turmaId;
    private String protocolo;
    private LocalDateTime dataInscricao;
    private double pontuacaoTotal;
    private int posicaoFila;
    private StatusInscricao status;
    private LocalDateTime dataChamada;
    private LocalDateTime createdAt;

    // Relacionamentos para uso em memória / consultas com JOIN
    private Aluno aluno;
    private Turma turma;
    private List<InscricaoCriterio> criterios;

    public Inscricao() {
        this.status = StatusInscricao.EM_ESPERA;
        this.criterios = new ArrayList<>();
    }

    public Inscricao(UUID alunoId, UUID turmaId) {
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.status = StatusInscricao.EM_ESPERA;
        this.dataInscricao = LocalDateTime.now();
        this.criterios = new ArrayList<>();
    }

    public Inscricao(UUID alunoId, UUID turmaId, String protocolo) {
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.protocolo = protocolo;
        this.status = StatusInscricao.EM_ESPERA;
        this.dataInscricao = LocalDateTime.now();
        this.criterios = new ArrayList<>();
    }

    public Inscricao(UUID id, UUID alunoId, UUID turmaId, String protocolo,
            LocalDateTime dataInscricao, double pontuacaoTotal, int posicaoFila,
            StatusInscricao status, LocalDateTime dataChamada, LocalDateTime createdAt) {
        this.id = id;
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.protocolo = protocolo;
        this.dataInscricao = dataInscricao;
        this.pontuacaoTotal = pontuacaoTotal;
        this.posicaoFila = posicaoFila;
        this.status = status != null ? status : StatusInscricao.EM_ESPERA;
        this.dataChamada = dataChamada;
        this.createdAt = createdAt;
        this.criterios = new ArrayList<>();
    }

    public void calcularPontuacao(List<CriterioPontuacaoStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            return;
        }
        double total = 0.0;
        for (CriterioPontuacaoStrategy strategy : strategies) {
            total += strategy.calcularPontos(this);
        }
        this.pontuacaoTotal = total;
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

    public UUID getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(UUID turmaId) {
        this.turmaId = turmaId;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(LocalDateTime dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public double getPontuacaoTotal() {
        return pontuacaoTotal;
    }

    public void setPontuacaoTotal(double pontuacaoTotal) {
        this.pontuacaoTotal = pontuacaoTotal;
    }

    public int getPosicaoFila() {
        return posicaoFila;
    }

    public void setPosicaoFila(int posicaoFila) {
        this.posicaoFila = posicaoFila;
    }

    public StatusInscricao getStatus() {
        return status;
    }

    public void setStatus(StatusInscricao status) {
        this.status = status;
    }

    public LocalDateTime getDataChamada() {
        return dataChamada;
    }

    public void setDataChamada(LocalDateTime dataChamada) {
        this.dataChamada = dataChamada;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public List<InscricaoCriterio> getCriterios() {
        return criterios;
    }

    public void setCriterios(List<InscricaoCriterio> criterios) {
        this.criterios = criterios;
    }

    @Override
    public String toString() {
        return "Inscricao{" +
                "id=" + id +
                ", alunoId=" + alunoId +
                ", turmaId=" + turmaId +
                ", protocolo='" + protocolo + '\'' +
                ", dataInscricao=" + dataInscricao +
                ", pontuacaoTotal=" + pontuacaoTotal +
                ", posicaoFila=" + posicaoFila +
                ", status=" + status +
                ", dataChamada=" + dataChamada +
                '}';
    }
}
