package com.sive.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Criterio {

    private UUID id;
    private String codigo;
    private String nome;
    private String descricao;
    private double pontosBase;
    private String categoria;
    private String tipoResposta;
    private boolean ativo;
    private int ordem;
    private LocalDateTime createdAt;

    public Criterio() {
        this.ativo = true;
        this.tipoResposta = "SIM_NAO";
        this.categoria = "SOCIAL";
    }

    public Criterio(String nome, double pontosBase, String categoria) {
        this();
        this.nome = nome;
        this.pontosBase = pontosBase;
        this.categoria = categoria;
    }

    public Criterio(UUID id, String nome, double pontosBase, String categoria, LocalDateTime createdAt) {
        this(nome, pontosBase, categoria);
        this.id = id;
        this.createdAt = createdAt;
    }

    public Criterio(
            UUID id,
            String nome,
            String descricao,
            double pontosBase,
            String categoria,
            String tipoResposta,
            boolean ativo,
            int ordem,
            LocalDateTime createdAt) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.pontosBase = pontosBase;
        this.categoria = categoria;
        this.tipoResposta = tipoResposta;
        this.ativo = ativo;
        this.ordem = ordem;
        this.createdAt = createdAt;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getTipoResposta() {
        return tipoResposta;
    }

    public void setTipoResposta(String tipoResposta) {
        this.tipoResposta = tipoResposta;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
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
                ", tipoResposta='" + tipoResposta + '\'' +
                ", ativo=" + ativo +
                ", ordem=" + ordem +
                ", createdAt=" + createdAt +
                '}';
    }
}
