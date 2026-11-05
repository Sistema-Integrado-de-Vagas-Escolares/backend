package com.sive.exception;

/**
 * Lancada quando uma transicao de status de inscricao ou entidade e invalida.
 */
public class StatusInvalidoException extends SiveException {

    public StatusInvalidoException(String message) {
        super(message);
    }
}

