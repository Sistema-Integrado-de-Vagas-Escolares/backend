-- =============================================================================
-- SIVE — Seed 02: Usuário Padrão (Escola autenticável)
-- Credenciais para desenvolvimento/teste:
--   codigo: EMEI-0001
--   senha:  sive123
-- Hash gerado no padrão PBKDF2WithHmacSHA256 (com.sive.security.PasswordHasher)
-- =============================================================================

INSERT INTO escolas (codigo, nome, bairro, cidade, capacidade_total, senha_hash) VALUES
    (
        'EMEI-0001',
        'EMEI Professora Maria Montessori',
        'Centro',
        'São Paulo',
        200,
        'f2Y7tYqjeShKShJlcrfCdg==:+hAVwXuA1R9fhSmEEG1GxI/k8AnVV7Ca0TE9fzWl6WU='
    )
ON CONFLICT (codigo) DO NOTHING;
