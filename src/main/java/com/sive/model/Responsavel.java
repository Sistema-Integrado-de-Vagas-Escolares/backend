package com.sive.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Responsavel extends Pessoa {

    private String cpf;
    private String telefone;
    private BigDecimal rendaFamiliar;

    public Responsavel() {
        super();
    }

    public Responsavel(String nome, String cpf, String telefone, BigDecimal rendaFamiliar) {
        super(nome);
        this.cpf = cpf;
        this.telefone = telefone;
        this.rendaFamiliar = rendaFamiliar;
    }

    public Responsavel(UUID id, String nome, String cpf, String telefone, BigDecimal rendaFamiliar) {
        super(id, nome);
        this.cpf = cpf;
        this.telefone = telefone;
        this.rendaFamiliar = rendaFamiliar;
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

    public BigDecimal getRendaFamiliar() {
        return rendaFamiliar;
    }

    public void setRendaFamiliar(BigDecimal rendaFamiliar) {
        this.rendaFamiliar = rendaFamiliar;
    }

    @Override
    public String toString() {
        return "Responsavel{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", cpf='" + cpf + '\'' +
                ", telefone='" + telefone + '\'' +
                ", rendaFamiliar=" + rendaFamiliar +
                '}';
    }
}
