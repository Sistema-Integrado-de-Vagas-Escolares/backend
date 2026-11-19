package com.sive.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sive.model.Turma;
import com.sive.service.TurmaService;

@RestController
@RequestMapping("/api")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    /**
     * Cadastra uma nova turma.
     * POST /api/turmas
     */
    @PostMapping("/turmas")
    public ResponseEntity<?> cadastrar(@RequestBody Turma turma) {
        try {
            Turma criada = turmaService.cadastrar(turma);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao acessar o banco de dados: " + e.getMessage());
        }
    }

    /**
     * Lista todas as turmas de uma escola específica.
     * GET /api/escolas/{escolaId}/turmas
     */
    @GetMapping("/escolas/{escolaId}/turmas")
    public ResponseEntity<?> listarPorEscola(@PathVariable UUID escolaId) {
        try {
            List<Turma> turmas = turmaService.listarPorEscola(escolaId);
            return ResponseEntity.ok(turmas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar turmas no banco de dados: " + e.getMessage());
        }
    }

    /**
     * Busca uma turma especifica por ID.
     * GET /api/turmas/{id}
     */
    @GetMapping("/turmas/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        try {
            Turma turma = turmaService.buscarPorId(id);
            if (turma == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Turma não encontrada para o ID informado.");
            }
            return ResponseEntity.ok(turma);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar turma no banco de dados: " + e.getMessage());
        }
    }
}
