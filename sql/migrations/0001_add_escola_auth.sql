-- =============================================================================
-- SIVE — Migration 0001: Autenticação de Escolas (código e senha)
-- =============================================================================

ALTER TABLE escolas
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(30),
    ADD COLUMN IF NOT EXISTS senha_hash VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_escolas_codigo'
    ) THEN
        ALTER TABLE escolas ADD CONSTRAINT uk_escolas_codigo UNIQUE (codigo);
    END IF;
END $$;
