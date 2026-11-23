package com.sive.controller;

import com.sive.model.ConsultaPublicaRequest;
import com.sive.model.ConsultaPublicaResponse;
import com.sive.service.ConsultaPublicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/publico/consulta")
public class ConsultaPublicaController {

    private final ConsultaPublicaService consultaPublicaService;

    public ConsultaPublicaController(ConsultaPublicaService consultaPublicaService) {
        this.consultaPublicaService = consultaPublicaService;
    }

    @PostMapping
    public ResponseEntity<ConsultaPublicaResponse> consultar(
            @RequestBody ConsultaPublicaRequest request) throws SQLException {
        return ResponseEntity.ok(consultaPublicaService.consultar(request));
    }

    @GetMapping("/protocolo/{protocolo}")
    public ResponseEntity<ConsultaPublicaResponse> consultarPorProtocolo(
            @PathVariable String protocolo) throws SQLException {
        return ResponseEntity.ok(consultaPublicaService.consultarPorProtocolo(protocolo));
    }
}
