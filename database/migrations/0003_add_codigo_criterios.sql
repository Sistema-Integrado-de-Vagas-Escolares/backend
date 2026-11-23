-- =============================================================================
-- SIVE — Migration 0003
-- Adiciona a coluna codigo na tabela criterios para vincular cada critério
-- configurado pela escola à sua Strategy de pontuação em com.sive.strategy.
-- =============================================================================

ALTER TABLE criterios
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(50);

UPDATE criterios SET codigo = 'RENDA' WHERE LOWER(nome) LIKE '%renda%' AND codigo IS NULL;
UPDATE criterios SET codigo = 'PCD' WHERE (LOWER(nome) LIKE '%pcd%' OR LOWER(nome) LIKE '%defici%') AND codigo IS NULL;
UPDATE criterios SET codigo = 'IRMAO' WHERE (LOWER(nome) LIKE '%irmão%' OR LOWER(nome) LIKE '%irmao%') AND codigo IS NULL;
UPDATE criterios SET codigo = 'VULNERABILIDADE' WHERE LOWER(nome) LIKE '%vulnerabilidade%' AND codigo IS NULL;
UPDATE criterios SET codigo = 'BPC_LOAS' WHERE (LOWER(nome) LIKE '%bpc%' OR LOWER(nome) LIKE '%loas%') AND codigo IS NULL;
UPDATE criterios SET codigo = 'BOLSA_FAMILIA' WHERE LOWER(nome) LIKE '%bolsa fam%' AND codigo IS NULL;
UPDATE criterios SET codigo = 'RESPONSAVEL_SOLO' WHERE (LOWER(nome) LIKE '%solo%' OR LOWER(nome) LIKE '%solteir%') AND codigo IS NULL;
UPDATE criterios SET codigo = 'MORA_ALUGUEL' WHERE LOWER(nome) LIKE '%aluguel%' AND codigo IS NULL;
UPDATE criterios SET codigo = 'PROXIMIDADE' WHERE LOWER(nome) LIKE '%proximidade%' AND codigo IS NULL;
