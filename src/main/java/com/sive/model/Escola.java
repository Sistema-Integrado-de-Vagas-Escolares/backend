package com.sive.model;

import java.util.UUID;

public class Escola {

    private UUID id;
    private String nome;
    private String bairro;
    private String cidade;
    private int capacidadeTotal;

    public Escola() {
    }

    public Escola(String nome, String bairro, String cidade, int capacidadeTotal) {
        this.nome = nome;
        this.bairro = bairro;
        this.cidade = cidade;
        this.capacidadeTotal = capacidadeTotal;
    }

    public Escola(UUID id, String nome, String bairro, String cidade, int capacidadeTotal) {
        this.id = id;
        this.nome = nome;
        this.bairro = bairro;
        this.cidade = cidade;
        this.capacidadeTotal = capacidadeTotal;
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

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public int getCapacidadeTotal() {
        return capacidadeTotal;
    }

    public void setCapacidadeTotal(int capacidadeTotal) {
        this.capacidadeTotal = capacidadeTotal;
    }

    @Override
    public String toString() {
        return "Escola{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", capacidadeTotal=" + capacidadeTotal +
                '}';
    }
}
