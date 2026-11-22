package com.sive.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Criterio {

    private UUID id;
    private String nome;
    private double pontosBase;
    private String categoria;
    private LocalDateTime createdAt;

    public Criterio() {
    }

    public Criterio(String nome, double pontosBase, String categoria) {
        this.nome = nome;
        this.pontosBase = pontosBase;
        this.categoria = categoria;
    }

    public Criterio(UUID id, String nome, double pontosBase, String categoria, LocalDateTime createdAt) {
        this.id = id;
        this.nome = nome;
        this.pontosBase = pontosBase;
        this.categoria = categoria;
        this.createdAt = createdAt;
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

    public double getPontosBase() {
        return pontosBase;
    }

    public void setPontosBase(double pontosBase) {
        this.pontosBase = pontosBase;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Criterio{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", pontosBase=" + pontosBase +
                ", categoria='" + categoria + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
