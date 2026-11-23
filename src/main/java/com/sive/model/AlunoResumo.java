package com.sive.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO com os dados resumidos do aluno e sua inscrição vinculada à escola.
 */
public class AlunoResumo {

    private UUID id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private Endereco endereco;
    private BigDecimal rendaPerCapita;
    private boolean temNecessidadeEspecial;
    private String etapaOuTurma;
    private String tipoVaga;
    private String turno;
    private BigDecimal pontuacaoTotal;
    private String statusInscricao;
    private String protocolo;

    public AlunoResumo() {
    }

    public AlunoResumo(
            UUID id,
            String nome,
            String cpf,
            LocalDate dataNascimento,
            Endereco endereco,
            BigDecimal rendaPerCapita,
            boolean temNecessidadeEspecial,
            String etapaOuTurma,
            String tipoVaga,
            String turno,
            BigDecimal pontuacaoTotal,
            String statusInscricao) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
        this.rendaPerCapita = rendaPerCapita;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
        this.etapaOuTurma = etapaOuTurma;
        this.tipoVaga = tipoVaga;
        this.turno = turno;
        this.pontuacaoTotal = pontuacaoTotal;
        this.statusInscricao = statusInscricao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public BigDecimal getRendaPerCapita() {
        return rendaPerCapita;
    }

    public void setRendaPerCapita(BigDecimal rendaPerCapita) {
        this.rendaPerCapita = rendaPerCapita;
    }

    public boolean isTemNecessidadeEspecial() {
        return temNecessidadeEspecial;
    }

    public void setTemNecessidadeEspecial(boolean temNecessidadeEspecial) {
        this.temNecessidadeEspecial = temNecessidadeEspecial;
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

    public BigDecimal getPontuacaoTotal() {
        return pontuacaoTotal;
    }

    public void setPontuacaoTotal(BigDecimal pontuacaoTotal) {
        this.pontuacaoTotal = pontuacaoTotal;
    }

    public String getStatusInscricao() {
        return statusInscricao;
    }

    public void setStatusInscricao(String statusInscricao) {
        this.statusInscricao = statusInscricao;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }
}
