package com.sive.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração Web MVC para registrar o interceptor de autenticação e as políticas de CORS.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EscolaAuthInterceptor escolaAuthInterceptor;
    private final String[] allowedOrigins;

    public WebConfig(
            EscolaAuthInterceptor escolaAuthInterceptor,
            @Value("${sive.cors.allowed-origins:http://localhost:3000,https://sive-frontend.gisellehoekveld.workers.dev}") String[] allowedOrigins) {
        this.escolaAuthInterceptor = escolaAuthInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(escolaAuthInterceptor)
                .addPathPatterns("/api/escola/**");
    }
}
