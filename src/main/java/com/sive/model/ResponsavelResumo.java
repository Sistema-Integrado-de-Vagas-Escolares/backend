package com.sive.model;

import java.util.UUID;

/**
 * DTO com dados cadastrais do responsável e quantidade de filhos vinculados.
 */
public class ResponsavelResumo {

    private UUID id;
    private String nome;
    private String cpf;
    private String telefone;
    private int totalFilhos;

    public ResponsavelResumo() {
    }

    public ResponsavelResumo(
            UUID id,
            String nome,
            String cpf,
            String telefone,
            int totalFilhos) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.totalFilhos = totalFilhos;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public int getTotalFilhos() {
        return totalFilhos;
    }

    public void setTotalFilhos(int totalFilhos) {
        this.totalFilhos = totalFilhos;
    }

    @Override
    public String toString() {
        return "ResponsavelResumo{"
                + "id=" + id
                + ", nome='" + nome + '\''
                + ", cpf='" + cpf + '\''
                + ", telefone='" + telefone + '\''
                + ", totalFilhos=" + totalFilhos
                + '}';
    }
}
