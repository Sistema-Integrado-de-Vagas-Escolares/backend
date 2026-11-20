package com.sive.controller;

import com.sive.model.Escola;
import com.sive.service.EscolaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

/**
 * Controlador REST público para cadastro de escolas.
 */
@RestController
@RequestMapping("/api/escolas")
public class EscolaController {

    private final EscolaService escolaService;

    public EscolaController(EscolaService escolaService) {
        this.escolaService = escolaService;
    }

    /**
     * Rota pública para cadastrar uma nova escola com código e senha de acesso.
     * POST /api/escolas
     */
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Escola escola) throws SQLException {
        Escola criada = escolaService.cadastrar(escola);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
}
