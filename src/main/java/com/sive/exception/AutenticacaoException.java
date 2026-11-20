package com.sive.exception;

/**
 * Exceção lançada quando credenciais de login ou token de acesso são inválidos ou expirados.
 */
public class AutenticacaoException extends SiveException {

    public AutenticacaoException(String message) {
        super(message);
    }
}
