package com.sive.model;

import java.util.UUID;

/**
 * Superclasse abstrata que representa uma pessoa no sistema SIVE.
 * Reune os atributos comuns a todas as entidades humanas do dominio:
 * identificador unico (UUID) e nome completo.
 *
 * <p>Esta classe nao deve ser instanciada diretamente. Utilize as subclasses
 * concretas: {@link Responsavel} e {@link Aluno}.</p>
 */
public abstract class Pessoa {

    private UUID id;
    private String nome;

    /**
     * Construtor padrao necessario para frameworks de serializacao (Jackson).
     */
    protected Pessoa() {
    }

    /**
     * Construtor para novas pessoas (sem ID gerado previamente).
     *
     * @param nome Nome completo da pessoa.
     */
    protected Pessoa(String nome) {
        this.nome = nome;
    }

    /**
     * Construtor completo para pessoas ja persistidas no banco de dados.
     *
     * @param id   Identificador unico (UUID) da pessoa.
     * @param nome Nome completo da pessoa.
     */
    protected Pessoa(UUID id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Pessoa{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}

