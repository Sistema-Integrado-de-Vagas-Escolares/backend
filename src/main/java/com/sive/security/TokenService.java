package com.sive.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sive.exception.AutenticacaoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço responsável pela geração e validação de tokens Bearer stateless assinados com HMAC-SHA256.
 */
@Service
public class TokenService {

    private static final String HMAC_ALGORITMO = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final byte[] secretBytes;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;

    public TokenService(
            @Value("${sive.auth.secret:sive-dev-secret-key-change-in-production-2026}") String secret,
            @Value("${sive.auth.expiration-seconds:86400}") long expirationSeconds) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("O segredo de autenticação (sive.auth.secret) não pode ser vazio.");
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
        this.objectMapper = new ObjectMapper();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    /**
     * Gera um token JWT assinado contendo o ID e o código da escola autenticada.
     */
    public String gerarToken(UUID escolaId, String codigoEscola) {
        long agora = Instant.now().getEpochSecond();
        long expiraEm = agora + expirationSeconds;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", escolaId.toString());
        payload.put("codigo", codigoEscola);
        payload.put("iat", agora);
        payload.put("exp", expiraEm);

        try {
            String headerB64 = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String dados = headerB64 + "." + payloadB64;
            String assinaturaB64 = base64UrlEncode(assinar(dados));
            return dados + "." + assinaturaB64;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar token de autenticação.", e);
        }
    }

    /**
     * Valida assinatura e expiração do token e retorna o UUID da escola autenticada.
     */
    public UUID validarEExtrairEscolaId(String token) {
        if (token == null || token.isBlank()) {
            throw new AutenticacaoException("Token de autenticação não informado.");
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new AutenticacaoException("Token de autenticação inválido.");
        }

        String dados = partes[0] + "." + partes[1];
        byte[] assinaturaEsperada = assinar(dados);
        byte[] assinaturaRecebida;
        try {
            assinaturaRecebida = Base64.getUrlDecoder().decode(partes[2]);
        } catch (IllegalArgumentException e) {
            throw new AutenticacaoException("Token de autenticação malformado.");
        }

        if (!MessageDigest.isEqual(assinaturaEsperada, assinaturaRecebida)) {
            throw new AutenticacaoException("Assinatura do token inválida.");
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(partes[1]);
            Map<String, Object> claims = objectMapper.readValue(payloadBytes, new TypeReference<>() {
            });

            Object expObj = claims.get("exp");
            if (!(expObj instanceof Number)) {
                throw new AutenticacaoException("Token sem data de expiração válida.");
            }
            long expiraEm = ((Number) expObj).longValue();
            if (Instant.now().getEpochSecond() > expiraEm) {
                throw new AutenticacaoException("Token de autenticação expirado.");
            }

            Object subObj = claims.get("sub");
            if (subObj == null) {
                throw new AutenticacaoException("Token sem identificador de escola.");
            }
            return UUID.fromString(subObj.toString());
        } catch (AutenticacaoException e) {
            throw e;
        } catch (Exception e) {
            throw new AutenticacaoException("Token de autenticação inválido.");
        }
    }

    private byte[] assinar(String dados) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITMO);
            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, HMAC_ALGORITMO);
            mac.init(keySpec);
            return mac.doFinal(dados.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Erro ao assinar token HMAC.", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
