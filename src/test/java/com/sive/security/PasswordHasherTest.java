package com.sive.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void deveGerarHashEVerificarSenhaCorreta() {
        String hash = PasswordHasher.hash("segredo123");

        assertNotNull(hash);
        assertTrue(hash.contains(":"));
        assertNotEquals("segredo123", hash);
        assertTrue(PasswordHasher.verificar("segredo123", hash));
        assertFalse(PasswordHasher.verificar("senhaErrada", hash));
    }

    @Test
    void deveGerarHashesDiferentesParaMesmaSenhaPorCausaDoSalt() {
        String hash1 = PasswordHasher.hash("mesmaSenha");
        String hash2 = PasswordHasher.hash("mesmaSenha");

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordHasher.verificar("mesmaSenha", hash1));
        assertTrue(PasswordHasher.verificar("mesmaSenha", hash2));
    }
}
