package com.sive.controller;

import com.sive.model.Escola;
import com.sive.service.EscolaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operacoes relacionadas as escolas.
 */
@RestController
@RequestMapping("/api/escolas")
public class EscolaController {

    private final EscolaService escolaService;

    public EscolaController(EscolaService escolaService) {
        this.escolaService = escolaService;
    }

    /**
     * Rota para cadastrar uma nova escola.
     * POST /api/escolas
     */
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Escola escola) {
        try {
            Escola criada = escolaService.cadastrar(escola);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao acessar o banco de dados: " + e.getMessage());
        }
    }

    /**
     * Rota para listar todas as escolas cadastradas.
     * GET /api/escolas
     */
    @GetMapping
    public ResponseEntity<?> listarTodas() {
        try {
            List<Escola> escolas = escolaService.listarTodas();
            return ResponseEntity.ok(escolas);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar escolas no banco de dados: " + e.getMessage());
        }
    }

    /**
     * Rota para buscar uma escola especifica por ID.
     * GET /api/escolas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        try {
            Escola escola = escolaService.buscarPorId(id);
            if (escola == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Escola nao encontrada para o ID informado.");
            }
            return ResponseEntity.ok(escola);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar escola no banco de dados: " + e.getMessage());
        }
    }
}
