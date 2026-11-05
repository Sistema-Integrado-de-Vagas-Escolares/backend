package com.sive.exception;

/**
 * Lancada quando uma solicitacao de vaga nao pode ser atendida por falta de vagas disponiveis.
 */
public class VagaIndisponivelException extends SiveException {

    public VagaIndisponivelException(String message) {
        super(message);
    }
}

