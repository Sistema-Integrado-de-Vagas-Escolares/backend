package com.sive.exception;

/**
 * Exceção lançada quando uma escola autenticada tenta acessar ou modificar recurso de outra escola.
 */
public class AcessoNegadoException extends SiveException {

    public AcessoNegadoException(String message) {
        super(message);
    }
}
