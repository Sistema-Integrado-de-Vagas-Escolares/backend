package com.sive.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilitário para geração e verificação de hash de senhas usando PBKDF2WithHmacSHA256 e salt aleatório.
 */
public final class PasswordHasher {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 65536;
    private static final int TAMANHO_CHAVE_BITS = 256;
    private static final int TAMANHO_SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * Gera um hash seguro no formato "saltBase64:hashBase64".
     */
    public static String hash(String senhaPura) {
        if (senhaPura == null || senhaPura.isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser vazia.");
        }

        byte[] salt = new byte[TAMANHO_SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derivarChave(senhaPura.toCharArray(), salt);

        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifica se a senha em texto puro corresponde ao hash armazenado em tempo constante.
     */
    public static boolean verificar(String senhaPura, String hashArmazenado) {
        if (senhaPura == null || hashArmazenado == null || !hashArmazenado.contains(":")) {
            return false;
        }

        String[] partes = hashArmazenado.split(":", 2);
        if (partes.length != 2) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(partes[0]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[1]);
            byte[] hashCalculado = derivarChave(senhaPura.toCharArray(), salt);
            return MessageDigest.isEqual(hashEsperado, hashCalculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivarChave(char[] senha, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(senha, salt, ITERACOES, TAMANHO_CHAVE_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro ao gerar hash da senha.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
