package com.sive.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sive.exception.AutenticacaoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Interceptor que valida o token Bearer nas rotas autenticadas da escola (/api/escola/**).
 */
@Component
public class EscolaAuthInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_ESCOLA_ID = "escolaIdAutenticada";

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public EscolaAuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            escreverErroNaoAutorizado(response, "Token Bearer não informado no cabeçalho Authorization.");
            return false;
        }

        String token = authHeader.substring(7).trim();
        try {
            UUID escolaId = tokenService.validarEExtrairEscolaId(token);
            request.setAttribute(ATRIBUTO_ESCOLA_ID, escolaId);
            return true;
        } catch (AutenticacaoException e) {
            escreverErroNaoAutorizado(response, e.getMessage());
            return false;
        }
    }

    private void escreverErroNaoAutorizado(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", HttpStatus.UNAUTHORIZED.value());
        corpo.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        corpo.put("message", mensagem);

        response.getWriter().write(objectMapper.writeValueAsString(corpo));
    }
}
