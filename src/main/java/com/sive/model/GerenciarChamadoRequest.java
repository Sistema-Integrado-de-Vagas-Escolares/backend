package com.sive.model;

import java.util.UUID;

/**
 * Payload para gerenciar uma inscrição com chamado em andamento.
 */
public class GerenciarChamadoRequest {

    private String acao;
    private UUID turmaId;

    public GerenciarChamadoRequest() {
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(UUID turmaId) {
        this.turmaId = turmaId;
    }
}
