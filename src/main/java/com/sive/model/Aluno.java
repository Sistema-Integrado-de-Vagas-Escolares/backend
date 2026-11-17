package com.sive.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade de dominio que representa o Aluno (crianca/estudante) no sistema SIVE.
 * Extende {@link Pessoa}, herdando o identificador unico e o nome.
 *
 * <p>Mapeada para a tabela {@code alunos} no banco de dados.</p>
 */
public class Aluno extends Pessoa {

    private LocalDate dataNascimento;
    private String certidaoNascimento;
    private boolean temNecessidadeEspecial;

    /**
     * Construtor padrao necessario para frameworks de serializacao (Jackson).
     */
    public Aluno() {
        super();
    }

    /**
     * Construtor para novos alunos (sem ID gerado previamente).
     *
     * @param nome                  Nome completo do aluno.
     * @param dataNascimento        Data de nascimento do aluno.
     * @param certidaoNascimento    Numero de registro da certidao de nascimento.
     * @param temNecessidadeEspecial Indica se o aluno possui deficiencia ou necessidade especial (PcD).
     */
    public Aluno(String nome, LocalDate dataNascimento, String certidaoNascimento,
            boolean temNecessidadeEspecial) {
        super(nome);
        this.dataNascimento = dataNascimento;
        this.certidaoNascimento = certidaoNascimento;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    /**
     * Construtor completo para alunos ja persistidos no banco de dados.
     *
     * @param id                    Identificador unico (UUID).
     * @param nome                  Nome completo do aluno.
     * @param dataNascimento        Data de nascimento do aluno.
     * @param certidaoNascimento    Numero de registro da certidao de nascimento.
     * @param temNecessidadeEspecial Indica se o aluno possui deficiencia ou necessidade especial (PcD).
     */
    public Aluno(UUID id, String nome, LocalDate dataNascimento, String certidaoNascimento,
            boolean temNecessidadeEspecial) {
        super(id, nome);
        this.dataNascimento = dataNascimento;
        this.certidaoNascimento = certidaoNascimento;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

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

