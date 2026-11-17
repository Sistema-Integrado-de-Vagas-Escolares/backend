package com.sive.controller;

import com.sive.model.Responsavel;
import com.sive.service.ResponsavelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/responsaveis")
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    public ResponsavelController(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Responsavel responsavel) {
        try {
            Responsavel criado = responsavelService.cadastrar(responsavel);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao acessar o banco de dados: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        try {
            List<Responsavel> responsaveis = responsavelService.listarTodos();
            return ResponseEntity.ok(responsaveis);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar responsaveis no banco de dados: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        try {
            Responsavel responsavel = responsavelService.buscarPorId(id);
            if (responsavel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Responsavel nao encontrado para o ID informado.");
            }
            return ResponseEntity.ok(responsavel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar responsavel no banco de dados: " + e.getMessage());
        }
    }
}
