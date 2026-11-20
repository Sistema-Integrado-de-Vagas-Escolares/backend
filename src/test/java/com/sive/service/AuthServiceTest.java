package com.sive.service;

import com.sive.dao.EscolaDAO;
import com.sive.exception.AutenticacaoException;
import com.sive.model.Escola;
import com.sive.model.LoginEscolaRequest;
import com.sive.model.LoginEscolaResponse;
import com.sive.security.PasswordHasher;
import com.sive.security.TokenService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void deveAutenticarEscolaComCodigoESenhaValidos() throws Exception {
        UUID escolaId = UUID.randomUUID();
        String hash = PasswordHasher.hash("senhaForte123");
        Escola escolaBanco = new Escola(escolaId, "EMEI-0012", "EMEI Teste", "Centro", "São Paulo", 100, hash);

        EscolaDAO daoStub = new EscolaDAO() {
            @Override
            public Escola buscarPorCodigo(String codigo) {
                return "EMEI-0012".equals(codigo) ? escolaBanco : null;
            }
        };

        TokenService tokenService = new TokenService("chave-secreta-teste-unitario-2026", 3600);
        AuthService authService = new AuthService(daoStub, tokenService);

        LoginEscolaResponse response = authService.autenticar(new LoginEscolaRequest("emei-0012", "senhaForte123"));

        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals(escolaId, response.getEscola().getId());
        assertNull(response.getEscola().getSenhaHash());
        assertEquals(escolaId, tokenService.validarEExtrairEscolaId(response.getToken()));
    }

    @Test
    void deveLancarAutenticacaoExceptionQuandoSenhaInvalida() {
        String hash = PasswordHasher.hash("senhaForte123");
        Escola escolaBanco = new Escola(UUID.randomUUID(), "EMEI-0012", "EMEI Teste", "Centro", "São Paulo", 100, hash);

        EscolaDAO daoStub = new EscolaDAO() {
            @Override
            public Escola buscarPorCodigo(String codigo) {
                return escolaBanco;
            }
        };

        TokenService tokenService = new TokenService("chave-secreta-teste-unitario-2026", 3600);
        AuthService authService = new AuthService(daoStub, tokenService);

        assertThrows(AutenticacaoException.class,
                () -> authService.autenticar(new LoginEscolaRequest("EMEI-0012", "senhaErrada")));
    }
}
