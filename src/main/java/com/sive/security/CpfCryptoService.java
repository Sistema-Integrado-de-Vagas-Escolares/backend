package com.sive.security;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utilitário para criptografia reversível (AES-256-GCM), hash determinístico (HMAC-SHA256)
 * e mascaramento de CPF para exibição segura (apenas 3 dígitos visíveis).
 */
public final class CpfCryptoService {

    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final String DEFAULT_DEV_SECRET = "sive-dev-cpf-secret-key-change-in-prod-2026";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CpfCryptoService() {
    }

    public static String normalizarDigitos(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }

    public static String formatar(String cpf) {
        String digits = normalizarDigitos(cpf);
        if (digits.length() != 11) {
            return cpf;
        }
        return digits.substring(0, 3) + "."
                + digits.substring(3, 6) + "."
                + digits.substring(6, 9) + "-"
                + digits.substring(9, 11);
    }

    /**
     * Gera um hash determinístico (HMAC-SHA256 em hex) dos 11 dígitos do CPF para buscas e unicidade.
     */
    public static String gerarHash(String cpf) {
        String digits = normalizarDigitos(cpf);
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("CPF inválido para geração de hash.");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(obterChaveBytes(), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(digits.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash do CPF.", e);
        }
    }

    /**
     * Criptografa o CPF formatado utilizando AES-256-GCM com IV aleatório.
     */
    public static String criptografar(String cpf) {
        String formatted = formatar(cpf);
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(obterChaveBytes(), "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] encrypted = cipher.doFinal(formatted.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criptografar CPF.", e);
        }
    }

    /**
     * Descriptografa um CPF armazenado com AES-256-GCM.
     */
    public static String descriptografar(String cpfCriptografado) {
        if (cpfCriptografado == null || cpfCriptografado.isBlank()) {
            return "";
        }
        // Fallback para registros legados não criptografados durante migração
        if (cpfCriptografado.matches("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")
                || cpfCriptografado.matches("^\\d{11}$")) {
            return cpfCriptografado;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(cpfCriptografado);
            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(obterChaveBytes(), "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao descriptografar CPF.", e);
        }
    }

    /**
     * Retorna apenas 3 dígitos do CPF mascarado no formato "***.XXX.***-**".
     */
    public static String mascararParaExibicao(String cpfArmazenado) {
        String cpfPlano = descriptografar(cpfArmazenado);
        String digits = normalizarDigitos(cpfPlano);
        if (digits.length() != 11) {
            return "***.***.***-**";
        }
        return "***." + digits.substring(3, 6) + ".***-**";
    }

    private static byte[] obterChaveBytes() {
        String secret = System.getenv("SIVE_CPF_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = DEFAULT_DEV_SECRET;
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao derivar chave criptográfica.", e);
        }
    }
}
