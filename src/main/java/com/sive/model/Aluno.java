package com.sive.model;

import java.time.LocalDate;
import java.util.UUID;

public class Aluno extends Pessoa {

    private LocalDate dataNascimento;
    private String certidaoNascimento;
    private boolean temNecessidadeEspecial;

    public Aluno() {
        super();
    }

    public Aluno(String nome, LocalDate dataNascimento, String certidaoNascimento,
            boolean temNecessidadeEspecial) {
        super(nome);
        this.dataNascimento = dataNascimento;
        this.certidaoNascimento = certidaoNascimento;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    public Aluno(UUID id, String nome, LocalDate dataNascimento, String certidaoNascimento,
            boolean temNecessidadeEspecial) {
        super(id, nome);
        this.dataNascimento = dataNascimento;
        this.certidaoNascimento = certidaoNascimento;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCertidaoNascimento() {
        return certidaoNascimento;
    }

    public void setCertidaoNascimento(String certidaoNascimento) {
        this.certidaoNascimento = certidaoNascimento;
    }

    public boolean isTemNecessidadeEspecial() {
        return temNecessidadeEspecial;
    }

    public void setTemNecessidadeEspecial(boolean temNecessidadeEspecial) {
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", certidaoNascimento='" + certidaoNascimento + '\'' +
                ", temNecessidadeEspecial=" + temNecessidadeEspecial +
                '}';
    }
}
