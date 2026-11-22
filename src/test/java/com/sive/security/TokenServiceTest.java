package com.sive.security;

import com.sive.exception.AutenticacaoException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    @Test
    void deveGerarEValidarTokenComSucesso() {
        TokenService tokenService = new TokenService("chave-secreta-teste-unitario-2026", 3600);
        UUID escolaId = UUID.randomUUID();

        String token = tokenService.gerarToken(escolaId, "EMEI-0012");
        UUID extraido = tokenService.validarEExtrairEscolaId(token);

        assertEquals(escolaId, extraido);
    }

    @Test
    void deveRejeitarTokenComAssinaturaAdulterada() {
        TokenService tokenService = new TokenService("chave-secreta-teste-unitario-2026", 3600);
        String token = tokenService.gerarToken(UUID.randomUUID(), "EMEI-0012");
        String tokenAdulterado = token.substring(0, token.length() - 2) + "xx";

        assertThrows(AutenticacaoException.class,
                () -> tokenService.validarEExtrairEscolaId(tokenAdulterado));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        TokenService tokenService = new TokenService("chave-secreta-teste-unitario-2026", -10);
        String token = tokenService.gerarToken(UUID.randomUUID(), "EMEI-0012");

        assertThrows(AutenticacaoException.class,
                () -> tokenService.validarEExtrairEscolaId(token));
    }
}
