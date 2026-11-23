package com.sive.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Aluno extends Pessoa {

    private String cpf;
    private LocalDate dataNascimento;
    private Endereco endereco;
    private BigDecimal rendaPerCapita;
    private boolean temNecessidadeEspecial;
    private boolean vulnerabilidadeSocioeconomica;
    private boolean responsavelSolo;
    private boolean moraDeAluguel;
    private boolean bolsaFamilia;
    private boolean bpcLoas;
    private boolean irmaoMatriculado;

    public Aluno() {
        super();
        this.rendaPerCapita = BigDecimal.ZERO;
    }

    public Aluno(String nome, String cpf, LocalDate dataNascimento, boolean temNecessidadeEspecial) {
        super(nome);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.rendaPerCapita = BigDecimal.ZERO;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    public Aluno(UUID id, String nome, String cpf, LocalDate dataNascimento, boolean temNecessidadeEspecial) {
        super(id, nome);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.rendaPerCapita = BigDecimal.ZERO;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    public Aluno(
            UUID id,
            String nome,
            String cpf,
            LocalDate dataNascimento,
            Endereco endereco,
            BigDecimal rendaPerCapita,
            boolean temNecessidadeEspecial) {
        super(id, nome);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
        this.rendaPerCapita = rendaPerCapita != null ? rendaPerCapita : BigDecimal.ZERO;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
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

    public boolean isVulnerabilidadeSocioeconomica() {
        return vulnerabilidadeSocioeconomica;
    }

    public void setVulnerabilidadeSocioeconomica(boolean vulnerabilidadeSocioeconomica) {
        this.vulnerabilidadeSocioeconomica = vulnerabilidadeSocioeconomica;
    }

    public boolean isResponsavelSolo() {
        return responsavelSolo;
    }

    public void setResponsavelSolo(boolean responsavelSolo) {
        this.responsavelSolo = responsavelSolo;
    }

    public boolean isMoraDeAluguel() {
        return moraDeAluguel;
    }

    public void setMoraDeAluguel(boolean moraDeAluguel) {
        this.moraDeAluguel = moraDeAluguel;
    }

    public boolean isBolsaFamilia() {
        return bolsaFamilia;
    }

    public void setBolsaFamilia(boolean bolsaFamilia) {
        this.bolsaFamilia = bolsaFamilia;
    }

    public boolean isBpcLoas() {
        return bpcLoas;
    }

    public void setBpcLoas(boolean bpcLoas) {
        this.bpcLoas = bpcLoas;
    }

    public boolean isIrmaoMatriculado() {
        return irmaoMatriculado;
    }

    public void setIrmaoMatriculado(boolean irmaoMatriculado) {
        this.irmaoMatriculado = irmaoMatriculado;
    }

    @Override
    public String toString() {
        return "Aluno{"
                + "id=" + getId()
                + ", nome='" + getNome() + '\''
                + ", cpf='" + cpf + '\''
                + ", dataNascimento=" + dataNascimento
                + ", endereco=" + endereco
                + ", rendaPerCapita=" + rendaPerCapita
                + ", temNecessidadeEspecial=" + temNecessidadeEspecial
                + '}';
    }
}
