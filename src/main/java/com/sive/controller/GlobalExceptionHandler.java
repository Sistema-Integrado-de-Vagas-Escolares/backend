package com.sive.controller;

import com.sive.exception.AlunoJaInscritoException;
import com.sive.exception.InscricaoNaoEncontradaException;
import com.sive.exception.SiveException;
import com.sive.exception.StatusInvalidoException;
import com.sive.exception.VagaIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interceptador global para tratamento centralizado de excecoes na API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> construirCorpoErro(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("error", status.getReasonPhrase());
        corpo.put("message", mensagem);
        return corpo;
    }

    @ExceptionHandler(InscricaoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(InscricaoNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(construirCorpoErro(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler({AlunoJaInscritoException.class, VagaIndisponivelException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(SiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(construirCorpoErro(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler({StatusInvalidoException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(construirCorpoErro(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(SiveException.class)
    public ResponseEntity<Map<String, Object>> handleGenericBusiness(SiveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(construirCorpoErro(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseError(SQLException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(construirCorpoErro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(construirCorpoErro(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado: " + ex.getMessage()));
    }
}

