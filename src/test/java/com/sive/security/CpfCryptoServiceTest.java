package com.sive.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfCryptoServiceTest {

    @Test
    void deveCriptografarEDescriptografarCpfComIvAleatorio() {
        String cpf = "123.456.789-00";
        String enc1 = CpfCryptoService.criptografar(cpf);
        String enc2 = CpfCryptoService.criptografar(cpf);

        assertNotEquals(cpf, enc1);
        assertNotEquals(enc1, enc2);
        assertEquals(cpf, CpfCryptoService.descriptografar(enc1));
        assertEquals(cpf, CpfCryptoService.descriptografar(enc2));
    }

    @Test
    void deveGerarHashDeterministicoIndependenteDeFormatacao() {
        String hashFormatado = CpfCryptoService.gerarHash("123.456.789-00");
        String hashLimpo = CpfCryptoService.gerarHash("12345678900");

        assertEquals(64, hashFormatado.length());
        assertEquals(hashFormatado, hashLimpo);
    }

    @Test
    void deveMascararCpfMostrandoApenasTresDigitos() {
        String criptografado = CpfCryptoService.criptografar("123.456.789-00");
        String mascarado = CpfCryptoService.mascararParaExibicao(criptografado);

        assertEquals("***.456.***-**", mascarado);
        assertEquals(3, mascarado.replaceAll("\\D", "").length());
    }
}
