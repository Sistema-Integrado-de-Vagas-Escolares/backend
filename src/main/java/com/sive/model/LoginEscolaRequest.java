package com.sive.model;

/**
 * DTO de requisição para autenticação de escola por código e senha.
 */
public class LoginEscolaRequest {

    private String codigo;
    private String senha;

    public LoginEscolaRequest() {
    }

    public LoginEscolaRequest(String codigo, String senha) {
        this.codigo = codigo;
        this.senha = senha;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
