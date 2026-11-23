-- =============================================================================
-- SIVE — Seed Consolidado (Ambiente: STAGING)
-- Inclui: Critérios iniciais, Escola EMEI-0001, Turmas, Responsáveis, Alunos,
--         Vínculos, Chamados/Inscrições e Histórico de Vagas.
--
-- Todos os UUIDs são v4 reais e os CPFs foram criptografados (AES-256-GCM)
-- e hashados (HMAC-SHA256) com a chave SIVE_CPF_SECRET de staging.
-- =============================================================================

-- 1. Critérios de Pontuação Iniciais (Soma ativa = 100%)
DELETE FROM criterios
WHERE nome IN (
    'Renda Familiar Baixa (até 1 SM)',
    'Necessidade Especial (PcD)',
    'Irmão Matriculado na Mesma Escola',
    'Proximidade Residencial (< 1 km)'
);

INSERT INTO criterios (id, codigo, nome, descricao, pontos_base, categoria, tipo_resposta, ativo, ordem) VALUES
    ('587e96cc-5215-4fbc-b222-2a81a94ebc71', 'RENDA', 'Renda familiar per capita', 'Pontuação máxima para renda até 1/4 do salário mínimo', 25.00, 'SOCIAL', 'FAIXA_VALORES', TRUE, 1),
    ('2e8cf901-8177-4184-a315-4bc2f90f54ac', 'PCD', 'Aluno com deficiência ou necessidade especial (PcD)', 'Laudo médico comprobatório', 20.00, 'SOCIAL', 'SIM_NAO', TRUE, 2),
    ('ba3d89a5-5262-4bbf-8b50-580125075121', 'IRMAO', 'Irmão matriculado na mesma escola', 'Criança com irmão já matriculado na mesma unidade escolar', 15.00, 'FAMILIAR', 'SIM_NAO', TRUE, 3),
    ('a74087a8-b60f-4550-9b70-e753063bfa54', 'VULNERABILIDADE', 'Vulnerabilidade socioeconômica', 'Família cadastrada no CadÚnico ou em situação de vulnerabilidade comprovada', 12.00, 'SOCIAL', 'SIM_NAO', TRUE, 4),
    ('082fad5b-6ccc-4f42-a338-5a7165a5e7cd', 'BPC_LOAS', 'Beneficiário do BPC/LOAS', 'Benefício de Prestação Continuada ativo', 10.00, 'SOCIAL', 'SIM_NAO', TRUE, 5),
    ('a2448741-5c6c-490e-89a2-99433cc11df7', 'BOLSA_FAMILIA', 'Beneficiário do Bolsa Família', 'Família inscrita e ativa no programa federal', 8.00, 'SOCIAL', 'SIM_NAO', TRUE, 6),
    ('1e6b5e00-ef4f-4b0d-9fb2-201c3b5a1a52', 'RESPONSAVEL_SOLO', 'Responsável solo', 'Mãe solteira, pai solteiro ou responsável único sem cônjuge', 6.00, 'FAMILIAR', 'SIM_NAO', TRUE, 7),
    ('2ae5ca02-994d-4bae-96ab-6e3c68870f40', 'MORA_ALUGUEL', 'Mora de aluguel', 'Família não possui imóvel próprio e paga aluguel', 4.00, 'SOCIAL', 'SIM_NAO', TRUE, 8)
ON CONFLICT (nome) DO UPDATE SET codigo = EXCLUDED.codigo;

-- 2. Escola Autenticável
INSERT INTO escolas (id, codigo, nome, bairro, cidade, capacidade_total, senha_hash) VALUES
    (
        '10fc4236-55de-4588-ad56-4868c2834057',
        'EMEI-0001',
        'EMEI Professora Maria Montessori',
        'Centro',
        'São Paulo',
        200,
        '+eXq5rZ85FsRMMRDg/2y5A==:OCXzpgagb3qrQKXlymRb6Hdh+BhdFmLuAbAhoNnppK0='
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
        ('39719b1e-e90f-4d95-ad8f-d8ec3107e66f', v_escola_id, 'Maternal I',  'Turma A', 'MANHA',    20, 3),
        ('639dde98-c34d-46c4-a265-897ec3f8d6c6', v_escola_id, 'Maternal II', 'Turma A', 'INTEGRAL', 20, 2),
        ('4dd6d593-b687-49e7-9fdb-089a9102010a', v_escola_id, 'Fase I',      'Turma A', 'TARDE',    25, 4),
        ('71770396-8580-4d39-8343-e54a79b85719', v_escola_id, 'Fase II',     'Turma A', 'MANHA',    25, 0),
        ('7024cd59-7c54-486c-b077-76758405803e', v_escola_id, 'Berçário',    'Turma A', 'INTEGRAL', 15, 2),
        ('39cb2f9e-83ef-433b-a26a-877460e374ef', v_escola_id, 'Maternal I',  'Turma A', 'TARDE',    20, 5)
    ON CONFLICT (escola_id, serie_ano, turno, nome) DO NOTHING;

    -- Responsáveis
    INSERT INTO responsaveis (id, nome, cpf, cpf_hash, telefone) VALUES
        ('e009b92f-66b4-49df-9b06-64e53315612c', 'Maria Oliveira', 'pRNmQcKjbIwBwBRHycXtVC2mwW6rTmHLoD/ErjS4LXjfdvpUmh9zS9/Y', '244454075158ec0e00ce8ac7a3bf2f08fdc599c347c96bcc44e156337531ff92', '(11) 99111-1111'),
        ('11653b45-80a3-42cd-b440-43c0e0dcf5ae', 'João Santos', 'eUOcpEW7SLvCwq2PZ+EioRmAL3bsgWOAk3iYRx6Xavs0JBmWyKq/2Vcj', '5194ccf45e00e6de4b55a10f5f07abbc621e1bf14ae1edc57441761b3bf63a69', '(11) 99222-2222'),
        ('8385a57c-95a9-4943-bf49-9d0506e5c954', 'Carla Ferreira', 'whTp/+H5qksosMbBonzH/jyBXohn0klbFYbjEVmyXrBecfMW58eFg0/j', 'b4fdca6a6d1c3caa8d3c00557c9faf2e1e91a0739872402623abfd0fa3f28490', '(11) 99333-3333'),
        ('677061d8-8997-4d64-a78d-eedf3831dea6', 'Roberto Lima', 'qXGuIq/Kn33T/3yB204IfGx9/eBpK8qFDt7DUkC0TWZbavTca0JvEbPz', '72153899a5ad8a57afda492dfe298f8d505f748daabdce5c7b359b73e6c7ad01', '(11) 99444-4444'),
        ('aded08f1-7cea-4b5e-91f3-75fdf951a6af', 'Ana Costa', 'su2NRRhGiEu5e1Ru8qCc17TaD/1WNXhAAiBc+8IHo5FJi2e5M+dfBGQm', 'b509f3160edf0e0433b1e33b07d7b46920cd652e54fef69022636d09304c13d4', '(11) 99555-5555')
    ON CONFLICT (cpf_hash) DO NOTHING;

    -- Alunos
    INSERT INTO alunos (id, nome, cpf, cpf_hash, data_nascimento, cep, rua, numero, complemento, bairro, cidade, estado, renda_per_capita, tem_necessidade_especial, vulnerabilidade_socioeconomica, responsavel_solo, mora_de_aluguel, bolsa_familia, bpc_loas, irmao_matriculado) VALUES
        ('0cbe60c2-4cd1-43ef-af8e-edd43e59d41f', 'Sofia Ferreira', '44bWuQZ3hnr3yQGnicQLOU05eyUsYyqBZL6o/IhIU9qpPryWzNUwk473', '56d3a4ec0599e29de1edf0e45cd273601b325753ffd7bc3ee8bcc82a7a0334bb', '2024-03-15', '01001-000', 'Praça da Sé', '100', NULL, 'Sé', 'São Paulo', 'SP', 550.00, FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE),
        ('4832c1a0-b260-4a8c-8dc2-31f5b8a9e2a3', 'Pedro Santos', '9fhTGXZGlO9k2YVn+KrJJulpMWYQeMZaix6+TvYYJCirG21ZrDF8ty+C', '22ad0932fd02cbc943c6453081a034c499fddee5fcc97549ade1feea39a0c969', '2023-08-10', '01310-100', 'Av. Paulista', '450', 'Apto 12', 'Bela Vista', 'São Paulo', 'SP', 680.00, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE),
        ('5e0db97c-d192-4ab2-b3e2-e6743a973951', 'Maria Lima', '4U3LijU7SUD1MyHx2JNOEuJoli7OIhjQSOlOm9+Avlz5c2as9SoYNelS', '641478d8abfd21ee723817c9fc4e4f525a2485174094130d97b1fa6fb9548adb', '2022-05-20', '01415-000', 'Rua Bela Cintra', '210', NULL, 'Consolação', 'São Paulo', 'SP', 1100.00, FALSE, TRUE, FALSE, FALSE, TRUE, TRUE, TRUE),
        ('f2e9321d-14a3-432b-9cd3-05200271d3c9', 'Lucas Oliveira', 'pVuLMirWOzXVPyzRsR9vPHEbHprBtP5BYmkN2bLjGwrcskFt2aFd7oLV', 'c66cdfeb4b90c2e550010c464baa32302ae2e744e85b64e0e5bb16ffd9d50b5c', '2024-06-01', '01501-000', 'Rua da Glória', '88', NULL, 'Liberdade', 'São Paulo', 'SP', 1500.00, FALSE, TRUE, TRUE, TRUE, FALSE, TRUE, FALSE),
        ('02051910-24db-45a5-938a-bd0b13a26e0d', 'Gabriel Costa', 'uLzLU+gUoonyCAEl64tz+ozrrRWFHanyeZbAcN4FRlICIN5zp1WPq6Wc', 'f19de2c9677b85619d3df72a750a28d3cf39c4eb7dc4efcff827d6c26092d974', '2024-01-12', '01227-200', 'Av. Angélica', '320', NULL, 'Santa Cecília', 'São Paulo', 'SP', 600.00, FALSE, TRUE, FALSE, TRUE, TRUE, TRUE, TRUE),
        ('e88b74e7-8de4-4009-a72a-02e22e421e17', 'Helena Souza', 'lcTY1kpVp8EVtlERz0UtsIgGLwIpCq1aCOgeSAJydauCwPEsCFjVrRMM', '7230bcc5f89d236de1db4a70ab5fbcb71be5feaa432e44bf9f849d27af0873e1', '2022-09-05', '01153-000', 'Rua Vitorino Carmilo', '55', 'Casa 2', 'Barra Funda', 'São Paulo', 'SP', 920.00, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE),
        ('ff991bbe-2b4a-4e72-aaba-1b854639ab71', 'Arthur Almeida', '1Xpe8WsNZ0uhQQwd+QHeVZnSx35bh/HPQ1G2zVimvZF/v8y7y5hrvBIR', '939e7c8569f0e7afd0ae643aeca30f6c09fd14efa889d75e3f33e5ba334886b9', '2024-02-22', '01031-000', 'Av. Prestes Maia', '700', NULL, 'Centro', 'São Paulo', 'SP', 700.00, FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE),
        ('c416e004-3eae-4045-a573-742a59707edf', 'Valentina Rocha', 'Db1WWCTjMw1PmMVucC/znPkKVVZojJcjta7A/PBPTBk6xz8g+3YG1imG', '80d815105028ea80f7928b51844bac159b71609c5cc2b7bd3ccaeb5ab244b260', '2021-11-18', '01323-000', 'Rua São Joaquim', '140', NULL, 'Liberdade', 'São Paulo', 'SP', 1800.00, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE)
    ON CONFLICT (cpf_hash) DO NOTHING;

    -- Vínculos Aluno - Responsável
    INSERT INTO aluno_responsaveis (id, aluno_id, responsavel_id, parentesco, is_contato_principal) VALUES
        ('545fcd72-5d60-467d-92fc-b00225798cfb', '0cbe60c2-4cd1-43ef-af8e-edd43e59d41f', '8385a57c-95a9-4943-bf49-9d0506e5c954', 'MAE', TRUE),
        ('e770d592-48f6-4594-8973-8af116bd6e41', '4832c1a0-b260-4a8c-8dc2-31f5b8a9e2a3', '11653b45-80a3-42cd-b440-43c0e0dcf5ae', 'PAI', TRUE),
        ('2c08ce80-6cd9-4ef5-8f7c-0abe10602018', '5e0db97c-d192-4ab2-b3e2-e6743a973951', '677061d8-8997-4d64-a78d-eedf3831dea6', 'PAI', TRUE),
        ('6493b906-1647-4f7a-8dee-10ee4c9a2471', 'f2e9321d-14a3-432b-9cd3-05200271d3c9', 'e009b92f-66b4-49df-9b06-64e53315612c', 'MAE', TRUE),
        ('f4c13a59-d21f-4a3d-a6aa-f5bf5bb198c5', '02051910-24db-45a5-938a-bd0b13a26e0d', 'aded08f1-7cea-4b5e-91f3-75fdf951a6af', 'MAE', TRUE),
        ('82297080-753c-43d3-95ef-196fd7958058', 'e88b74e7-8de4-4009-a72a-02e22e421e17', 'e009b92f-66b4-49df-9b06-64e53315612c', 'MAE', TRUE),
        ('a5a2ffe3-482f-4de7-960e-81e45371a9cc', 'ff991bbe-2b4a-4e72-aaba-1b854639ab71', '11653b45-80a3-42cd-b440-43c0e0dcf5ae', 'PAI', TRUE),
        ('586f3842-fbed-48d8-a276-7c761d23a4d6', 'c416e004-3eae-4045-a573-742a59707edf', '8385a57c-95a9-4943-bf49-9d0506e5c954', 'MAE', TRUE)
    ON CONFLICT (aluno_id, responsavel_id) DO NOTHING;

    -- Inscrições / Chamados com estados variados (escala 0-100)
    INSERT INTO inscricoes (id, aluno_id, escola_id, turma_id, tipo_vaga, serie_ano, turno, protocolo, data_inscricao, pontuacao_total, status, data_chamada) VALUES
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', '0cbe60c2-4cd1-43ef-af8e-edd43e59d41f', v_escola_id, NULL, 'CRECHE', 'Maternal I',  'MANHA',    'SIVE-STG0001', CURRENT_TIMESTAMP - INTERVAL '10 days', 80.00, 'EM_ESPERA',   NULL),
        ('141c2777-8e67-407c-ac8c-d760bdaf702b', '4832c1a0-b260-4a8c-8dc2-31f5b8a9e2a3', v_escola_id, NULL, 'CRECHE', 'Maternal II', 'INTEGRAL', 'SIVE-STG0002', CURRENT_TIMESTAMP - INTERVAL '9 days',  72.00, 'EM_ESPERA',   NULL),
        ('6769c5d1-7d0e-443b-94a3-6d6ecc485a2d', '5e0db97c-d192-4ab2-b3e2-e6743a973951', v_escola_id, NULL, 'CRECHE', 'Fase I',      'TARDE',    'SIVE-STG0003', CURRENT_TIMESTAMP - INTERVAL '8 days',  60.00, 'EM_ESPERA',   NULL),
        ('7683d9a1-7340-49a0-843f-5f33f5c6d828', 'f2e9321d-14a3-432b-9cd3-05200271d3c9', v_escola_id, NULL, 'CRECHE', 'Maternal I',  'INTEGRAL', 'SIVE-STG0004', CURRENT_TIMESTAMP - INTERVAL '7 days',  40.00, 'EM_ESPERA',   NULL),
        ('655bf925-bb05-4f60-bd14-253bdb611426', '02051910-24db-45a5-938a-bd0b13a26e0d', v_escola_id, NULL, 'CRECHE', 'Maternal I',  'MANHA',    'SIVE-STG0005', CURRENT_TIMESTAMP - INTERVAL '12 days', 74.00, 'CHAMADO',     CURRENT_TIMESTAMP - INTERVAL '1 day'),
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', 'e88b74e7-8de4-4009-a72a-02e22e421e17', v_escola_id, NULL, 'CRECHE', 'Fase I',      'TARDE',    'SIVE-STG0006', CURRENT_TIMESTAMP - INTERVAL '11 days', 75.00, 'CHAMADO',     CURRENT_TIMESTAMP - INTERVAL '2 days'),
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', 'ff991bbe-2b4a-4e72-aaba-1b854639ab71', v_escola_id, '39719b1e-e90f-4d95-ad8f-d8ec3107e66f', 'CRECHE', 'Maternal I', 'MANHA', 'SIVE-STG0007', CURRENT_TIMESTAMP - INTERVAL '15 days', 80.00, 'MATRICULADO', CURRENT_TIMESTAMP - INTERVAL '5 days'),
        ('d8ee7202-dfeb-46f9-9d4f-5662ff87975f', 'c416e004-3eae-4045-a573-742a59707edf', v_escola_id, NULL, 'CRECHE', 'Fase II',     'MANHA',    'SIVE-STG0008', CURRENT_TIMESTAMP - INTERVAL '20 days', 35.00, 'EXPIRADO',    CURRENT_TIMESTAMP - INTERVAL '10 days')
    ON CONFLICT (protocolo) DO NOTHING;

    -- Detalhamento dos critérios pontuados por inscrição (consistente com pontuacao_total)
    INSERT INTO inscricao_criterios (inscricao_id, criterio_id, pontos_obtidos) VALUES
        -- Sofia (80.00)
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('6cf163c9-7520-43e7-834b-9e5d0fad87b7', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Pedro (72.00)
        ('141c2777-8e67-407c-ac8c-d760bdaf702b', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('141c2777-8e67-407c-ac8c-d760bdaf702b', (SELECT id FROM criterios WHERE codigo = 'PCD'), 20.00),
        ('141c2777-8e67-407c-ac8c-d760bdaf702b', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('141c2777-8e67-407c-ac8c-d760bdaf702b', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        -- Maria (60.00)
        ('6769c5d1-7d0e-443b-94a3-6d6ecc485a2d', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 15.00),
        ('6769c5d1-7d0e-443b-94a3-6d6ecc485a2d', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('6769c5d1-7d0e-443b-94a3-6d6ecc485a2d', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('6769c5d1-7d0e-443b-94a3-6d6ecc485a2d', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('6769c5d1-7d0e-443b-94a3-6d6ecc485a2d', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        -- Lucas (40.00)
        ('7683d9a1-7340-49a0-843f-5f33f5c6d828', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 8.00),
        ('7683d9a1-7340-49a0-843f-5f33f5c6d828', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('7683d9a1-7340-49a0-843f-5f33f5c6d828', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('7683d9a1-7340-49a0-843f-5f33f5c6d828', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('7683d9a1-7340-49a0-843f-5f33f5c6d828', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Gabriel (74.00)
        ('655bf925-bb05-4f60-bd14-253bdb611426', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('655bf925-bb05-4f60-bd14-253bdb611426', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('655bf925-bb05-4f60-bd14-253bdb611426', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('655bf925-bb05-4f60-bd14-253bdb611426', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('655bf925-bb05-4f60-bd14-253bdb611426', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('655bf925-bb05-4f60-bd14-253bdb611426', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Helena (75.00)
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 15.00),
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', (SELECT id FROM criterios WHERE codigo = 'PCD'), 20.00),
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('9f5fee8f-1c39-4a41-8879-79998b75c6cf', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Arthur (80.00)
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 25.00),
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00),
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', (SELECT id FROM criterios WHERE codigo = 'BPC_LOAS'), 10.00),
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', (SELECT id FROM criterios WHERE codigo = 'BOLSA_FAMILIA'), 8.00),
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', (SELECT id FROM criterios WHERE codigo = 'RESPONSAVEL_SOLO'), 6.00),
        ('07413f09-c9f8-4c5f-b930-c105c05ac056', (SELECT id FROM criterios WHERE codigo = 'MORA_ALUGUEL'), 4.00),
        -- Valentina (35.00)
        ('d8ee7202-dfeb-46f9-9d4f-5662ff87975f', (SELECT id FROM criterios WHERE codigo = 'RENDA'), 8.00),
        ('d8ee7202-dfeb-46f9-9d4f-5662ff87975f', (SELECT id FROM criterios WHERE codigo = 'IRMAO'), 15.00),
        ('d8ee7202-dfeb-46f9-9d4f-5662ff87975f', (SELECT id FROM criterios WHERE codigo = 'VULNERABILIDADE'), 12.00)
    ON CONFLICT (inscricao_id, criterio_id) DO NOTHING;

    -- Histórico inicial de atualizações de vagas
    INSERT INTO historico_vagas (id, escola_id, turma_id, vagas_anteriores, vagas_novas, motivo, created_at) VALUES
        ('4d2f201a-673e-4509-88f4-9a2655330bc3', v_escola_id, '39719b1e-e90f-4d95-ad8f-d8ec3107e66f', 1, 3, 'Cancelamento de matrícula', CURRENT_TIMESTAMP - INTERVAL '2 days'),
        ('9b5ece41-b56c-4d8c-8301-523102d74496', v_escola_id, '4dd6d593-b687-49e7-9fdb-089a9102010a', 6, 4, 'Novas matrículas',          CURRENT_TIMESTAMP - INTERVAL '5 days')
    ON CONFLICT (id) DO NOTHING;
END $$;
