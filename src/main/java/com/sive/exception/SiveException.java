package com.sive.exception;

/**
 * Excecao base para erros de regras de negocio do sistema SIVE.
 */
public class SiveException extends RuntimeException {

    public SiveException(String message) {
        super(message);
    }

    public SiveException(String message, Throwable cause) {
        super(message, cause);
    }
}

