-- =============================================================================
-- SIVE — Sistema Integrado de Vagas Escolares (Backend MVP)
-- Schema DDL para PostgreSQL 16 (Suporte a N:N Aluno - Responsaveis)
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Tabela de Escolas
CREATE TABLE IF NOT EXISTS escolas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nome VARCHAR(150) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    capacidade_total INTEGER NOT NULL CHECK (capacidade_total > 0),
    senha_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Turmas
CREATE TABLE IF NOT EXISTS turmas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    escola_id UUID NOT NULL REFERENCES escolas(id) ON DELETE CASCADE,
    serie_ano VARCHAR(50) NOT NULL,
    nome VARCHAR(60) NOT NULL DEFAULT 'Turma A',
    turno VARCHAR(20) NOT NULL CHECK (turno IN ('MANHA', 'TARDE', 'INTEGRAL')),
    vagas_totais INTEGER NOT NULL CHECK (vagas_totais >= 0),
    vagas_disponiveis INTEGER NOT NULL CHECK (vagas_disponiveis >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_vagas CHECK (vagas_disponiveis <= vagas_totais),
    CONSTRAINT uk_turma_escola_serie_turno_nome UNIQUE (escola_id, serie_ano, turno, nome)
);

-- 3. Tabela de Responsáveis
CREATE TABLE IF NOT EXISTS responsaveis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(255) NOT NULL,
    cpf_hash VARCHAR(64) NOT NULL UNIQUE,
    telefone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabela de Alunos
CREATE TABLE IF NOT EXISTS alunos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(255) NOT NULL,
    cpf_hash VARCHAR(64) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    cep VARCHAR(9),
    rua VARCHAR(150),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado CHAR(2),
    renda_per_capita NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    tem_necessidade_especial BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tabela Associativa Aluno - Responsáveis (N:N)
CREATE TABLE IF NOT EXISTS aluno_responsaveis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aluno_id UUID NOT NULL REFERENCES alunos(id) ON DELETE CASCADE,
    responsavel_id UUID NOT NULL REFERENCES responsaveis(id) ON DELETE CASCADE,
    parentesco VARCHAR(30) NOT NULL DEFAULT 'MAE' 
        CHECK (parentesco IN ('MAE', 'PAI', 'AVOS', 'TUTOR_LEGAL', 'OUTRO')),
    is_contato_principal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_aluno_responsavel UNIQUE (aluno_id, responsavel_id)
);

-- 6. Tabela de Critérios de Pontuação
CREATE TABLE IF NOT EXISTS criterios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT NOT NULL DEFAULT '',
    pontos_base NUMERIC(5, 2) NOT NULL CHECK (pontos_base >= 0),
    categoria VARCHAR(30) NOT NULL CHECK (categoria IN ('SOCIAL', 'GEOGRAFICO', 'FAMILIAR')),
    tipo_resposta VARCHAR(30) NOT NULL DEFAULT 'SIM_NAO' CHECK (tipo_resposta IN ('FAIXA_VALORES', 'SIM_NAO', 'DISTANCIA')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tabela de Inscrições na Fila de Espera
CREATE TABLE IF NOT EXISTS inscricoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aluno_id UUID NOT NULL REFERENCES alunos(id) ON DELETE CASCADE,
    escola_id UUID REFERENCES escolas(id) ON DELETE CASCADE,
    turma_id UUID REFERENCES turmas(id) ON DELETE CASCADE,
    tipo_vaga VARCHAR(20) CHECK (tipo_vaga IN ('CRECHE', 'FUNDAMENTAL')),
    serie_ano VARCHAR(50),
    turno VARCHAR(20) CHECK (turno IN ('MANHA', 'TARDE', 'INTEGRAL')),
    protocolo VARCHAR(30) NOT NULL UNIQUE,
    data_inscricao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pontuacao_total NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ESPERA' 
        CHECK (status IN ('EM_ESPERA', 'CHAMADO', 'MATRICULADO', 'CANCELADO', 'EXPIRADO')),
    data_chamada TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_aluno_turma UNIQUE (aluno_id, turma_id)
);

-- 8. Tabela de Detalhamento dos Critérios da Inscrição (N:N com nota)
CREATE TABLE IF NOT EXISTS inscricao_criterios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inscricao_id UUID NOT NULL REFERENCES inscricoes(id) ON DELETE CASCADE,
    criterio_id UUID NOT NULL REFERENCES criterios(id) ON DELETE CASCADE,
    pontos_obtidos NUMERIC(5, 2) NOT NULL CHECK (pontos_obtidos >= 0),
    CONSTRAINT uk_inscricao_criterio UNIQUE (inscricao_id, criterio_id)
);

-- 9. Tabela de Histórico de Atualizações de Vagas
CREATE TABLE IF NOT EXISTS historico_vagas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    escola_id UUID NOT NULL REFERENCES escolas(id) ON DELETE CASCADE,
    turma_id UUID NOT NULL REFERENCES turmas(id) ON DELETE CASCADE,
    vagas_anteriores INTEGER NOT NULL CHECK (vagas_anteriores >= 0),
    vagas_novas INTEGER NOT NULL CHECK (vagas_novas >= 0),
    motivo VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
