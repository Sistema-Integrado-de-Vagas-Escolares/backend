package com.sive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Escola {

    private UUID id;
    private String codigo;
    private String nome;
    private String bairro;
    private String cidade;
    private int capacidadeTotal;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @JsonIgnore
    private String senhaHash;

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

    public Escola(UUID id, String codigo, String nome, String bairro, String cidade, int capacidadeTotal) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.bairro = bairro;
        this.cidade = cidade;
        this.capacidadeTotal = capacidadeTotal;
    }

    public Escola(UUID id, String codigo, String nome, String bairro, String cidade, int capacidadeTotal,
            String senhaHash) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.bairro = bairro;
        this.cidade = cidade;
        this.capacidadeTotal = capacidadeTotal;
        this.senhaHash = senhaHash;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    @Override
    public String toString() {
        return "Escola{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nome='" + nome + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", capacidadeTotal=" + capacidadeTotal +
                '}';
    }
}
