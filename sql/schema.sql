-- =============================================================================
-- SIVE — Sistema Integrado de Vagas Escolares (Backend MVP)
-- Schema DDL para PostgreSQL 16
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Tabela de Escolas
CREATE TABLE IF NOT EXISTS escolas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    capacidade_total INTEGER NOT NULL CHECK (capacidade_total > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Turmas
CREATE TABLE IF NOT EXISTS turmas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    escola_id UUID NOT NULL REFERENCES escolas(id) ON DELETE CASCADE,
    serie_ano VARCHAR(50) NOT NULL,
    turno VARCHAR(20) NOT NULL CHECK (turno IN ('MANHA', 'TARDE', 'INTEGRAL')),
    vagas_totais INTEGER NOT NULL CHECK (vagas_totais >= 0),
    vagas_disponiveis INTEGER NOT NULL CHECK (vagas_disponiveis >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_vagas CHECK (vagas_disponiveis <= vagas_totais)
);

-- 3. Tabela de Responsáveis
CREATE TABLE IF NOT EXISTS responsaveis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    telefone VARCHAR(20) NOT NULL,
    renda_familiar NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabela de Alunos
CREATE TABLE IF NOT EXISTS alunos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    responsavel_id UUID NOT NULL REFERENCES responsaveis(id) ON DELETE CASCADE,
    nome VARCHAR(150) NOT NULL,
    data_nascimento DATE NOT NULL,
    certidao_nascimento VARCHAR(50) NOT NULL,
    tem_necessidade_especial BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tabela de Critérios de Pontuação
CREATE TABLE IF NOT EXISTS criterios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL UNIQUE,
    pontos_base NUMERIC(5, 2) NOT NULL CHECK (pontos_base >= 0),
    categoria VARCHAR(30) NOT NULL CHECK (categoria IN ('SOCIAL', 'GEOGRAFICO', 'FAMILIAR')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Tabela de Inscrições na Fila de Espera
CREATE TABLE IF NOT EXISTS inscricoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aluno_id UUID NOT NULL REFERENCES alunos(id) ON DELETE CASCADE,
    turma_id UUID NOT NULL REFERENCES turmas(id) ON DELETE CASCADE,
    protocolo VARCHAR(30) NOT NULL UNIQUE,
    data_inscricao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pontuacao_total NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    posicao_fila INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ESPERA' 
        CHECK (status IN ('EM_ESPERA', 'CHAMADO', 'MATRICULADO', 'CANCELADO', 'EXPIRADO')),
    data_chamada TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_aluno_turma UNIQUE (aluno_id, turma_id)
);

-- 7. Tabela de Detalhamento dos Critérios da Inscrição (N:N com nota)
CREATE TABLE IF NOT EXISTS inscricao_criterios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inscricao_id UUID NOT NULL REFERENCES inscricoes(id) ON DELETE CASCADE,
    criterio_id UUID NOT NULL REFERENCES criterios(id) ON DELETE CASCADE,
    pontos_obtidos NUMERIC(5, 2) NOT NULL CHECK (pontos_obtidos >= 0),
    CONSTRAINT uk_inscricao_criterio UNIQUE (inscricao_id, criterio_id)
);

-- =============================================================================
-- Inserção de Dados Iniciais de Teste (Seed)
-- =============================================================================

INSERT INTO criterios (nome, pontos_base, categoria) VALUES
    ('Renda Familiar Baixa (até 1 SM)', 40.00, 'SOCIAL'),
    ('Necessidade Especial (PcD)', 30.00, 'SOCIAL'),
    ('Irmão Matriculado na Mesma Escola', 20.00, 'FAMILIAR'),
    ('Proximidade Residencial (< 1 km)', 10.00, 'GEOGRAFICO')
ON CONFLICT (nome) DO NOTHING;

