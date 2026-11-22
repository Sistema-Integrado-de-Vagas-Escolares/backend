package com.sive.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sive.dao.Conexao;

/**
 * Endpoint de verificação de integridade e status da aplicação (Health Check).
 * Acessível via /health-check, /health ou /api/health.
 */
@RestController
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping({"/health-check", "/health", "/api/health"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("status", "UP");
        resposta.put("app", "sive-backend");
        resposta.put("timestamp", LocalDateTime.now().toString());

        Map<String, Object> database = new LinkedHashMap<>();
        try (Connection conn = Conexao.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                database.put("status", "UP");
                database.put("mensagem", "Conexão com PostgreSQL ativa");
                database.put("catalogo", conn.getCatalog());
            } else {
                database.put("status", "DOWN");
                database.put("mensagem", "Conexão nula ou fechada");
            }
        } catch (SQLException e) {
            database.put("status", "DOWN");
            database.put("mensagem", "Falha ao conectar: " + e.getMessage());
        }

        resposta.put("database", database);

        return ResponseEntity.ok(resposta);
    }
}
