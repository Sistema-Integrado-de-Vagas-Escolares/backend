-- =============================================================================
-- SIVE — Seed 01: Critérios de Pontuação Iniciais
-- =============================================================================

INSERT INTO criterios (nome, pontos_base, categoria) VALUES
    ('Renda Familiar Baixa (até 1 SM)', 40.00, 'SOCIAL'),
    ('Necessidade Especial (PcD)', 30.00, 'SOCIAL'),
    ('Irmão Matriculado na Mesma Escola', 20.00, 'FAMILIAR'),
    ('Proximidade Residencial (< 1 km)', 10.00, 'GEOGRAFICO')
ON CONFLICT (nome) DO NOTHING;
