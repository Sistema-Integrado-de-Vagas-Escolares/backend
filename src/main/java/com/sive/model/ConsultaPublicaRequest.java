package com.sive.model;

/**
 * Payload de autenticação pública do responsável por CPF e protocolo do chamado.
 */
public class ConsultaPublicaRequest {

    private String cpf;
    private String protocolo;

    public ConsultaPublicaRequest() {
    }

    public ConsultaPublicaRequest(String cpf, String protocolo) {
        this.cpf = cpf;
        this.protocolo = protocolo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }
}
