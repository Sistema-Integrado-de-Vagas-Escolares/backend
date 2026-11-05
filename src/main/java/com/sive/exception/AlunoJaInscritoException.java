package com.sive.exception;

/**
 * Lancada quando se tenta inscrever um aluno ja ativo em uma fila ou turma conflitante.
 */
public class AlunoJaInscritoException extends SiveException {

    public AlunoJaInscritoException(String message) {
        super(message);
    }
}

