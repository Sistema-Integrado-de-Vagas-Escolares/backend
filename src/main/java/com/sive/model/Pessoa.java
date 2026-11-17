package com.sive.model;

import java.util.UUID;

public abstract class Pessoa {

    private UUID id;
    private String nome;

    protected Pessoa() {
    }

    protected Pessoa(String nome) {
        this.nome = nome;
    }

    protected Pessoa(UUID id, String nome) {
        this.id = id;
        this.nome = nome;
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

    @Override
    public String toString() {
        return "Pessoa{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}
