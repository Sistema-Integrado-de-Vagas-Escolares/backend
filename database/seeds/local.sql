-- =============================================================================
-- SIVE — Seed Consolidado (Ambiente: LOCAL)
-- Inclui: Critérios iniciais, Escola EMEI-0001, Turmas, Responsáveis, Alunos,
--         Vínculos, Chamados/Inscrições e Histórico de Vagas.
--
-- Credenciais padrão para desenvolvimento local:
--   codigo: EMEI-0001
--   senha:  sive123
-- =============================================================================

-- 1. Critérios de Pontuação Iniciais (Soma ativa = 100%)
DELETE FROM criterios
WHERE nome IN (
    'Renda Familiar Baixa (até 1 SM)',
    'Necessidade Especial (PcD)',
    'Irmão Matriculado na Mesma Escola',
    'Proximidade Residencial (< 1 km)'
);

INSERT INTO criterios (codigo, nome, descricao, pontos_base, categoria, tipo_resposta, ativo, ordem) VALUES
    ('RENDA', 'Renda familiar per capita', 'Pontuação máxima para renda até 1/4 do salário mínimo', 25.00, 'SOCIAL', 'FAIXA_VALORES', TRUE, 1),
    ('PCD', 'Aluno com deficiência ou necessidade especial (PcD)', 'Laudo médico comprobatório', 20.00, 'SOCIAL', 'SIM_NAO', TRUE, 2),
    ('IRMAO', 'Irmão matriculado na mesma escola', 'Criança com irmão já matriculado na mesma unidade escolar', 15.00, 'FAMILIAR', 'SIM_NAO', TRUE, 3),
    ('VULNERABILIDADE', 'Vulnerabilidade socioeconômica', 'Família cadastrada no CadÚnico ou em situação de vulnerabilidade comprovada', 12.00, 'SOCIAL', 'SIM_NAO', TRUE, 4),
    ('BPC_LOAS', 'Beneficiário do BPC/LOAS', 'Benefício de Prestação Continuada ativo', 10.00, 'SOCIAL', 'SIM_NAO', TRUE, 5),
    ('BOLSA_FAMILIA', 'Beneficiário do Bolsa Família', 'Família inscrita e ativa no programa federal', 8.00, 'SOCIAL', 'SIM_NAO', TRUE, 6),
    ('RESPONSAVEL_SOLO', 'Responsável solo', 'Mãe solteira, pai solteiro ou responsável único sem cônjuge', 6.00, 'FAMILIAR', 'SIM_NAO', TRUE, 7),
    ('MORA_ALUGUEL', 'Mora de aluguel', 'Família não possui imóvel próprio e paga aluguel', 4.00, 'SOCIAL', 'SIM_NAO', TRUE, 8)
ON CONFLICT (nome) DO UPDATE SET codigo = EXCLUDED.codigo;

-- 2. Escola Autenticável
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

-- 3. Turmas, Responsáveis, Alunos, Inscrições e Histórico de Vagas
DO $$
DECLARE
    v_escola_id UUID;
BEGIN
    SELECT id INTO v_escola_id FROM escolas WHERE codigo = 'EMEI-0001' LIMIT 1;
    IF v_escola_id IS NULL THEN
        RAISE EXCEPTION 'Escola EMEI-0001 não encontrada.';
    END IF;

    -- Turmas da escola (125 de 200 vagas alocadas)
    INSERT INTO turmas (id, escola_id, serie_ano, nome, turno, vagas_totais, vagas_disponiveis) VALUES
        ('10000000-0000-0000-0000-000000000001', v_escola_id, 'Maternal I',  'Turma A', 'MANHA',    20, 3),
        ('10000000-0000-0000-0000-000000000002', v_escola_id, 'Maternal II', 'Turma A', 'INTEGRAL', 20, 2),
        ('10000000-0000-0000-0000-000000000003', v_escola_id, 'Fase I',      'Turma A', 'TARDE',    25, 4),
        ('10000000-0000-0000-0000-000000000004', v_escola_id, 'Fase II',     'Turma A', 'MANHA',    25, 0),
        ('10000000-0000-0000-0000-000000000005', v_escola_id, 'Berçário',    'Turma A', 'INTEGRAL', 15, 2),
        ('10000000-0000-0000-0000-000000000006', v_escola_id, 'Maternal I',  'Turma A', 'TARDE',    20, 5)
    ON CONFLICT (id) DO NOTHING;

    -- Responsáveis
    INSERT INTO responsaveis (id, nome, cpf, cpf_hash, telefone) VALUES
        ('20000000-0000-0000-0000-000000000001', 'Maria Oliveira', '111.222.333-44', 'aac258df458c5ceef30733d3333f98ed6d1225181eac16aca441e7e6564caa86', '(11) 99111-1111'),
        ('20000000-0000-0000-0000-000000000002', 'João Santos',    '222.333.444-55', 'c9bd821e4ae0118fcdf555203494d4707a0054608f23f51b4d4105923d60ccad', '(11) 99222-2222'),
        ('20000000-0000-0000-0000-000000000003', 'Carla Ferreira', '333.444.555-66', '93ce029c879b28e1c807dad28ef804f96ad36809232afdd0eb06fe588b6b83b7', '(11) 99333-3333'),
        ('20000000-0000-0000-0000-000000000004', 'Roberto Lima',   '444.555.666-77', '56b5f75a826b7827b901bfe4b93f8705e7bc235735ca38906548d34d8d4c9a6a', '(11) 99444-4444'),
        ('20000000-0000-0000-0000-000000000005', 'Ana Costa',      '555.666.777-88', '03a3964e5358dea4f1df7896604e728662c28e5b3d8d67bd654e692f65fbffa6', '(11) 99555-5555')
    ON CONFLICT (id) DO NOTHING;

    -- Alunos
    INSERT INTO alunos (id, nome, cpf, cpf_hash, data_nascimento, cep, rua, numero, complemento, bairro, cidade, estado, renda_per_capita, tem_necessidade_especial, vulnerabilidade_socioeconomica, responsavel_solo, mora_de_aluguel, bolsa_familia, bpc_loas, irmao_matriculado) VALUES
        ('30000000-0000-0000-0000-000000000001', 'Sofia Ferreira',  '666.777.888-99', '3a82ac7ccc887aefc8b74c572e8f50d23a50491af79cca6376dea22c8af2383f', '2024-03-15', '01001-000', 'Praça da Sé',         '100', NULL,      'Sé',            'São Paulo', 'SP', 550.00,  FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE),
        ('30000000-0000-0000-0000-000000000002', 'Pedro Santos',    '777.888.999-00', '8ddadfaba7019a0e4b2751b2385e53886dee6da19557e498ee51044251a6ba44', '2023-08-10', '01310-100', 'Av. Paulista',        '450', 'Apto 12', 'Bela Vista',    'São Paulo', 'SP', 680.00,  TRUE,  TRUE, FALSE, FALSE, FALSE, FALSE, TRUE),
        ('30000000-0000-0000-0000-000000000003', 'Maria Lima',      '888.999.000-11', '6c491e8a78c3088415ec30ef547c4086e8aefe6bbdf999b34048a6b993b30bae', '2022-05-20', '01415-000', 'Rua Bela Cintra',     '210', NULL,      'Consolação',    'São Paulo', 'SP', 1100.00, FALSE, TRUE, FALSE, FALSE, TRUE, TRUE, TRUE),
        ('30000000-0000-0000-0000-000000000004', 'Lucas Oliveira',  '999.000.111-22', '2b69477e3861261abc249d35f2305e3c7139ca820ba6dcaa654c0ae6e299b433', '2024-06-01', '01501-000', 'Rua da Glória',       '88',  NULL,      'Liberdade',     'São Paulo', 'SP', 1500.00, FALSE, TRUE, TRUE, TRUE, FALSE, TRUE, FALSE),
        ('30000000-0000-0000-0000-000000000005', 'Gabriel Costa',   '123.123.123-00', 'ca3116eb481442909f6909e11a5a068929efbf1df76701e6a9613748fc54dc90', '2024-01-12', '01227-200', 'Av. Angélica',        '320', NULL,      'Santa Cecília', 'São Paulo', 'SP', 600.00,  FALSE, TRUE, FALSE, TRUE, TRUE, TRUE, TRUE),
        ('30000000-0000-0000-0000-000000000006', 'Helena Souza',    '234.234.234-00', '4b675f33676b71f3bd7e4ca4c29c12e2716ab12131df5b642907dc51797cc8b7', '2022-09-05', '01153-000', 'Rua Vitorino Carmilo','55',  'Casa 2',  'Barra Funda',   'São Paulo', 'SP', 920.00,  TRUE,  TRUE, TRUE, TRUE, TRUE, TRUE, FALSE),
        ('30000000-0000-0000-0000-000000000007', 'Arthur Almeida',  '345.345.345-00', 'f127d3397e2a80d23c71eb521a517ca2932392c56a54eb53f49bfbfab47f4d31', '2024-02-22', '01031-000', 'Av. Prestes Maia',    '700', NULL,      'Centro',        'São Paulo', 'SP', 700.00,  FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE),
        ('30000000-0000-0000-0000-000000000008', 'Valentina Rocha', '456.456.456-00', '79a134ecf11cb49de3fda22779baf1bfddaf2c1d63b9f829cb4adfe6770c74a3', '2021-11-18', '01323-000', 'Rua São Joaquim',     '140', NULL,      'Liberdade',     'São Paulo', 'SP', 1800.00, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE)
    ON CONFLICT (id) DO NOTHING;

    -- Vínculos Aluno - Responsável
    INSERT INTO aluno_responsaveis (aluno_id, responsavel_id, parentesco, is_contato_principal) VALUES
        ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', 'MAE', TRUE),
        ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'PAI', TRUE),
        ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000004', 'PAI', TRUE),
        ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'MAE', TRUE),
        ('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'MAE', TRUE),
        ('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000001', 'MAE', TRUE),
        ('30000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000002', 'PAI', TRUE),
        ('30000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000003', 'MAE', TRUE)
    ON CONFLICT (aluno_id, responsavel_id) DO NOTHING;

    -- Inscrições / Chamados com estados variados (escala 0-100)
    INSERT INTO inscricoes (id, aluno_id, escola_id, turma_id, tipo_vaga, serie_ano, turno, protocolo, data_inscricao, pontuacao_total, status, data_chamada) VALUES
        ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', v_escola_id, NULL, 'CRECHE', 'Maternal I',  'MANHA',    'SIVE-SIM0001', CURRENT_TIMESTAMP - INTERVAL '10 days', 80.00, 'EM_ESPERA',   NULL),
        ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', v_escola_id, NULL, 'CRECHE', 'Maternal II', 'INTEGRAL', 'SIVE-SIM0002', CURRENT_TIMESTAMP - INTERVAL '9 days',  72.00, 'EM_ESPERA',   NULL),
        ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', v_escola_id, NULL, 'CRECHE', 'Fase I',      'TARDE',    'SIVE-SIM0003', CURRENT_TIMESTAMP - INTERVAL '8 days',  60.00, 'EM_ESPERA',   NULL),
        ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000004', v_escola_id, NULL, 'CRECHE', 'Maternal I',  'INTEGRAL', 'SIVE-SIM0004', CURRENT_TIMESTAMP - INTERVAL '7 days',  40.00, 'EM_ESPERA',   NULL),
        ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000005', v_escola_id, NULL, 'CRECHE', 'Maternal I',  'MANHA',    'SIVE-SIM0005', CURRENT_TIMESTAMP - INTERVAL '12 days', 74.00, 'CHAMADO',     CURRENT_TIMESTAMP - INTERVAL '1 day'),
        ('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000006', v_escola_id, NULL, 'CRECHE', 'Fase I',      'TARDE',    'SIVE-SIM0006', CURRENT_TIMESTAMP - INTERVAL '11 days', 75.00, 'CHAMADO',     CURRENT_TIMESTAMP - INTERVAL '2 days'),
        ('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000007', v_escola_id, '10000000-0000-0000-0000-000000000001', 'CRECHE', 'Maternal I', 'MANHA', 'SIVE-SIM0007', CURRENT_TIMESTAMP - INTERVAL '15 days', 80.00, 'MATRICULADO', CURRENT_TIMESTAMP - INTERVAL '5 days'),
        ('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000008', v_escola_id, NULL, 'CRECHE', 'Fase II',     'MANHA',    'SIVE-SIM0008', CURRENT_TIMESTAMP - INTERVAL '20 days', 35.00, 'EXPIRADO',    CURRENT_TIMESTAMP - INTERVAL '10 days')
    ON CONFLICT (id) DO NOTHING;

    -- Detalhamento dos critérios pontuados por inscrição (consistente com pontuacao_total)
    INSERT INTO inscricao_criterios (inscricao_id, criterio_id, pontos_obtidos) VALUES
        -- Sofia (80.00)
        ('40000000-0000-0000-0000-000000000001', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('40000000-0000-0000-0000-000000000001', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('40000000-0000-0000-0000-000000000001', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('40000000-0000-0000-0000-000000000001', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('40000000-0000-0000-0000-000000000001', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('40000000-0000-0000-0000-000000000001', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('40000000-0000-0000-0000-000000000001', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Pedro (72.00)
        ('40000000-0000-0000-0000-000000000002', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('40000000-0000-0000-0000-000000000002', (SELECT id FROM criterios WHERE codigo = 'PCD'), 20.00),
        ('40000000-0000-0000-0000-000000000002', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('40000000-0000-0000-0000-000000000002', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        -- Maria (60.00)
        ('40000000-0000-0000-0000-000000000003', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 15.00),
        ('40000000-0000-0000-0000-000000000003', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('40000000-0000-0000-0000-000000000003', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('40000000-0000-0000-0000-000000000003', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('40000000-0000-0000-0000-000000000003', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        -- Lucas (40.00)
        ('40000000-0000-0000-0000-000000000004', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 8.00),
        ('40000000-0000-0000-0000-000000000004', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('40000000-0000-0000-0000-000000000004', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('40000000-0000-0000-0000-000000000004', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('40000000-0000-0000-0000-000000000004', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Gabriel (74.00)
        ('40000000-0000-0000-0000-000000000005', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('40000000-0000-0000-0000-000000000005', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('40000000-0000-0000-0000-000000000005', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('40000000-0000-0000-0000-000000000005', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('40000000-0000-0000-0000-000000000005', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('40000000-0000-0000-0000-000000000005', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Helena (75.00)
        ('40000000-0000-0000-0000-000000000006', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 15.00),
        ('40000000-0000-0000-0000-000000000006', (SELECT id FROM criterios WHERE codigo = 'PCD'), 20.00),
        ('40000000-0000-0000-0000-000000000006', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('40000000-0000-0000-0000-000000000006', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('40000000-0000-0000-0000-000000000006', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('40000000-0000-0000-0000-000000000006', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('40000000-0000-0000-0000-000000000006', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Arthur (80.00)
        ('40000000-0000-0000-0000-000000000007', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('40000000-0000-0000-0000-000000000007', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('40000000-0000-0000-0000-000000000007', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('40000000-0000-0000-0000-000000000007', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('40000000-0000-0000-0000-000000000007', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('40000000-0000-0000-0000-000000000007', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('40000000-0000-0000-0000-000000000007', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Valentina (35.00)
        ('40000000-0000-0000-0000-000000000008', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 8.00),
        ('40000000-0000-0000-0000-000000000008', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('40000000-0000-0000-0000-000000000008', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00)
    ON CONFLICT (inscricao_id, criterio_id) DO NOTHING;

    -- Histórico inicial de atualizações de vagas
    INSERT INTO historico_vagas (id, escola_id, turma_id, vagas_anteriores, vagas_novas, motivo, created_at) VALUES
        ('50000000-0000-0000-0000-000000000001', v_escola_id, '10000000-0000-0000-0000-000000000001', 1, 3, 'Cancelamento de matrícula', CURRENT_TIMESTAMP - INTERVAL '2 days'),
        ('50000000-0000-0000-0000-000000000002', v_escola_id, '10000000-0000-0000-0000-000000000003', 6, 4, 'Novas matrículas',          CURRENT_TIMESTAMP - INTERVAL '5 days')
    ON CONFLICT (id) DO NOTHING;
END $$;
