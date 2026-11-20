package com.sive.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração Web MVC para registrar o interceptor de autenticação das rotas da escola.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EscolaAuthInterceptor escolaAuthInterceptor;

    public WebConfig(EscolaAuthInterceptor escolaAuthInterceptor) {
        this.escolaAuthInterceptor = escolaAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(escolaAuthInterceptor)
                .addPathPatterns("/api/escola/**");
    }
}
