package com.sive.exception;

/**
 * Lancada quando um registro de inscricao nao e encontrado pelo identificador ou protocolo.
 */
public class InscricaoNaoEncontradaException extends SiveException {

    public InscricaoNaoEncontradaException(String message) {
        super(message);
    }
}

