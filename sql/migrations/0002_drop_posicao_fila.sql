-- =============================================================================
-- SIVE — Migration 0002: Remover coluna posicao_fila da tabela inscricoes
-- =============================================================================

ALTER TABLE inscricoes DROP COLUMN IF EXISTS posicao_fila;

