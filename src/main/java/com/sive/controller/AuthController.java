package com.sive.controller;

import com.sive.model.LoginEscolaRequest;
import com.sive.model.LoginEscolaResponse;
import com.sive.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

/**
 * Controlador REST público para autenticação de escolas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Realiza o login da escola utilizando código (ex: EMEI-0012) e senha.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginEscolaResponse> login(@RequestBody LoginEscolaRequest request) throws SQLException {
        LoginEscolaResponse response = authService.autenticar(request);
        return ResponseEntity.ok(response);
    }
}
