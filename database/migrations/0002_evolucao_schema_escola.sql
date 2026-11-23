-- =============================================================================
-- SIVE — Migration 0002 (Consolidada: 0002 a 0008)
-- 1. Responsáveis e Alunos: CPF criptografado + cpf_hash, endereço estruturado e renda_per_capita
-- 2. Inscrições: turma_id opcional, escola_id, tipo_vaga, serie_ano, turno e remoção de posicao_fila
-- 3. Critérios: descrição, tipo_resposta, ativo e ordem
-- 4. Turmas: identificador/sala (nome) e unicidade por escola/série/turno/nome
-- 5. Histórico de Vagas: tabela historico_vagas e índice por escola/data
-- =============================================================================

-- 1. Responsáveis
ALTER TABLE responsaveis
    DROP COLUMN IF EXISTS renda_familiar;

ALTER TABLE responsaveis
    DROP CONSTRAINT IF EXISTS responsaveis_cpf_key;

ALTER TABLE responsaveis
    ALTER COLUMN cpf TYPE VARCHAR(255);

ALTER TABLE responsaveis
    ADD COLUMN IF NOT EXISTS cpf_hash VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uk_responsaveis_cpf_hash
    ON responsaveis (cpf_hash);

-- 2. Alunos
ALTER TABLE alunos
    DROP COLUMN IF EXISTS certidao_nascimento,
    DROP COLUMN IF EXISTS endereco;

ALTER TABLE alunos
    ADD COLUMN IF NOT EXISTS cep VARCHAR(9),
    ADD COLUMN IF NOT EXISTS rua VARCHAR(150),
    ADD COLUMN IF NOT EXISTS numero VARCHAR(20),
    ADD COLUMN IF NOT EXISTS complemento VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bairro VARCHAR(100),
    ADD COLUMN IF NOT EXISTS cidade VARCHAR(100),
    ADD COLUMN IF NOT EXISTS estado CHAR(2),
    ADD COLUMN IF NOT EXISTS renda_per_capita NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS vulnerabilidade_socioeconomica BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS responsavel_solo BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS mora_de_aluguel BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS bolsa_familia BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS bpc_loas BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS irmao_matriculado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS cpf VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cpf_hash VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uk_alunos_cpf_hash
    ON alunos (cpf_hash);

-- 3. Inscrições
ALTER TABLE inscricoes
    ALTER COLUMN turma_id DROP NOT NULL;

ALTER TABLE inscricoes
    ADD COLUMN IF NOT EXISTS escola_id UUID REFERENCES escolas(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS tipo_vaga VARCHAR(20) CHECK (tipo_vaga IN ('CRECHE', 'FUNDAMENTAL')),
    ADD COLUMN IF NOT EXISTS serie_ano VARCHAR(50),
    ADD COLUMN IF NOT EXISTS turno VARCHAR(20) CHECK (turno IN ('MANHA', 'TARDE', 'INTEGRAL'));

ALTER TABLE inscricoes
    DROP COLUMN IF EXISTS posicao_fila;

-- 4. Critérios
ALTER TABLE criterios
    ADD COLUMN IF NOT EXISTS descricao TEXT NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS tipo_resposta VARCHAR(30) NOT NULL DEFAULT 'SIM_NAO'
        CHECK (tipo_resposta IN ('FAIXA_VALORES', 'SIM_NAO', 'DISTANCIA')),
    ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS ordem INTEGER NOT NULL DEFAULT 1;

-- 5. Turmas
ALTER TABLE turmas
    ADD COLUMN IF NOT EXISTS nome VARCHAR(60) NOT NULL DEFAULT 'Turma A';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_turma_escola_serie_turno_nome'
    ) THEN
        ALTER TABLE turmas
            ADD CONSTRAINT uk_turma_escola_serie_turno_nome
            UNIQUE (escola_id, serie_ano, turno, nome);
    END IF;
END $$;

-- 6. Histórico de Atualizações de Vagas
CREATE TABLE IF NOT EXISTS historico_vagas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    escola_id UUID NOT NULL REFERENCES escolas(id) ON DELETE CASCADE,
    turma_id UUID NOT NULL REFERENCES turmas(id) ON DELETE CASCADE,
    vagas_anteriores INTEGER NOT NULL CHECK (vagas_anteriores >= 0),
    vagas_novas INTEGER NOT NULL CHECK (vagas_novas >= 0),
    motivo VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_historico_vagas_escola_data
    ON historico_vagas (escola_id, created_at DESC);
