package com.sive.model;

import java.util.UUID;

public class Responsavel extends Pessoa {

    private String cpf;
    private String telefone;

    public Responsavel() {
        super();
    }

    public Responsavel(String nome, String cpf, String telefone) {
        super(nome);
        this.cpf = cpf;
        this.telefone = telefone;
    }

    public Responsavel(UUID id, String nome, String cpf, String telefone) {
        super(id, nome);
        this.cpf = cpf;
        this.telefone = telefone;
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

    @Override
    public String toString() {
        return "Responsavel{"
                + "id=" + getId()
                + ", nome='" + getNome() + '\''
                + ", cpf='" + cpf + '\''
                + ", telefone='" + telefone + '\''
                + '}';
    }
}
