# SIVE — Sistema Integrado de Vagas Escolares (Backend)

O **SIVE** é uma aplicação voltada para a gestão transparente, organizada e automatizada de solicitações de vagas e filas de espera na educação infantil e ensino fundamental.

O sistema permite cadastrar escolas e turmas, receber inscrições de dependentes vinculados a responsáveis legais, pontuar as solicitações com base em critérios socioeconômicos e geográficos configuráveis, e gerenciar a convocação e matrícula dos alunos de forma concorrente e consistente.

---

## 1. Tecnologias Utilizadas

- **Linguagem:** Java 17+
- **Framework Web:** Spring Boot (Spring Web para rotas e controladores REST)
- **Banco de Dados:** PostgreSQL 16
- **Acesso a Dados:** JDBC Nativo (`java.sql`) com transações manuais
- **Containerização:** Docker e Docker Compose

---

## 2. Arquitetura e Conceitos Centrais

A aplicação adota uma arquitetura em camadas orientada a objetos:

```text
HTTP Request ──► Controller (REST) ──► Service (Regras de Negócio) ──► DAO (JDBC) ──► PostgreSQL
                                            │
                             ┌──────────────┴──────────────┐
                             ▼                             ▼
                  Strategy (Pontuação)          Tarefa (Threads Concorrentes)
```

### Padrão Strategy (Motor de Pontuação)
A pontuação de cada inscrição na fila varia de acordo com critérios sociais e geográficos. O sistema utiliza o padrão **Strategy** para desacoplar as regras de cálculo:
- `CriterioPontuacaoStrategy` (Interface base): define o contrato `double calcularPontos(Inscricao inscricao)`.
- Implementações concretas:
  - `CriterioRendaStrategy`: pontua inscrições com base na renda familiar per capita.
  - `CriterioProximidadeStrategy`: pontua a proximidade residencial do aluno em relação à escola.
  - `CriterioIrmaoStrategy`: concede pontuação adicional para alunos com irmãos matriculados na mesma escola.
  - `CriterioPcdStrategy`: aplica prioridade legal para candidatos com deficiência/necessidades especiais.
- `CriterioStrategyFactory`: fabrica e entrega a estratégia adequada para cada critério avaliado.

### Threads e Concorrência
O sistema opera em ambiente concorrente e aborda duas frentes:
1. **Processamento Concorrente de Filas (`tarefa`):** Operações demoradas, como recálculo de pontuação em lote ou geração de métricas de demanda, são executadas por tarefas independentes que implementam `Runnable` e rodam em `Thread` separadas, permitindo sincronização com `start()` e `join()`.
2. **Proteção contra Condições de Corrida (*Race Conditions*):** Métodos críticos de reserva e alocação de vagas utilizam bloqueios sincronizados (`synchronized`) e controle transacional no banco para garantir que duas requisições simultâneas não preencham a mesma vaga além da capacidade da turma.

### Tratamento Centralizado de Exceções
Regras de negócio violadas lançam exceções customizadas do pacote `exception` (ex.: `VagaIndisponivelException`, `AlunoJaInscritoException`, `InscricaoNaoEncontradaException`). O controlador `GlobalExceptionHandler` intercepta esses erros e retorna mensagens padronizadas em JSON com os devidos códigos de status HTTP (400, 404, 409).

---

## 3. Estrutura de Pastas e Pacotes

```text
backend/
├── docker-compose.yml              # Orquestração do PostgreSQL
├── pom.xml                         # Dependências do projeto Maven
├── README.md                       # Documentação técnica do projeto
├── sql/
│   └── schema.sql                  # Script DDL com a estrutura das tabelas
└── src/main/java/com/sive/
    ├── SiveApplication.java        # Inicializador da aplicação
    │
    ├── controller/                 # Controladores REST da API
    │   ├── EscolaController.java
    │   ├── TurmaController.java
    │   ├── ResponsavelController.java
    │   ├── InscricaoController.java
    │   └── GlobalExceptionHandler.java
    │
    ├── service/                    # Regras de negócio e coordenação de fluxo
    │   ├── InscricaoService.java
    │   ├── TurmaService.java
    │   └── FilaService.java
    │
    ├── strategy/                   # Implementações do padrão Strategy
    │   ├── CriterioPontuacaoStrategy.java
    │   ├── CriterioRendaStrategy.java
    │   ├── CriterioProximidadeStrategy.java
    │   ├── CriterioIrmaoStrategy.java
    │   ├── CriterioPcdStrategy.java
    │   └── CriterioStrategyFactory.java
    │
    ├── tarefa/                     # Tarefas concorrentes (Threads / Runnable)
    │   ├── TarefaRecalcularFila.java
    │   ├── TarefaCalcularEstatisticas.java
    │   └── TarefaNotificarChamada.java
    │
    ├── dao/                        # Acesso a dados via JDBC nativo
    │   ├── Conexao.java
    │   ├── EscolaDAO.java
    │   ├── TurmaDAO.java
    │   ├── ResponsavelDAO.java
    │   ├── AlunoDAO.java
    │   └── InscricaoDAO.java
    │
    ├── model/                      # Classes de domínio
    │   ├── Pessoa.java             # Superclasse com atributos comuns
    │   ├── Responsavel.java        # Dados cadastrais do responsável legal
    │   ├── Aluno.java              # Dados da criança/estudante
    │   ├── Escola.java             # Unidade escolar
    │   ├── Turma.java              # Turma, turno e saldo de vagas
    │   ├── Inscricao.java          # Inscrição na fila de espera
    │   ├── StatusInscricao.java    # Enum: EM_ESPERA, CHAMADO, MATRICULADO, etc.
    │   └── Turno.java              # Enum: MANHA, TARDE, INTEGRAL
    │
    └── exception/                  # Exceções de negócio
        ├── SiveException.java
        ├── VagaIndisponivelException.java
        ├── AlunoJaInscritoException.java
        ├── InscricaoNaoEncontradaException.java
        └── StatusInvalidoException.java
```

---

## 4. Modelagem do Banco de Dados

O banco relacional é composto por 7 tabelas normalizadas:

```mermaid
erDiagram
    ESCOLAS ||--o{ TURMAS : "oferta"
    RESPONSAVEIS ||--o{ ALUNOS : "possui"
    ALUNOS ||--o{ INSCRICOES : "candidata-se"
    TURMAS ||--o{ INSCRICOES : "recebe"
    INSCRICOES ||--o{ INSCRICAO_CRITERIOS : "detalha"
    CRITERIOS ||--o{ INSCRICAO_CRITERIOS : "pontua"

    ESCOLAS {
        uuid id PK
        varchar nome
        varchar bairro
        varchar cidade
        integer capacidade_total
    }

    TURMAS {
        uuid id PK
        uuid escola_id FK
        varchar serie_ano
        varchar turno
        integer vagas_totais
        integer vagas_disponiveis
    }

    RESPONSAVEIS {
        uuid id PK
        varchar nome
        varchar cpf UK
        varchar telefone
        numeric renda_familiar
    }

    ALUNOS {
        uuid id PK
        uuid responsavel_id FK
        varchar nome
        date data_nascimento
        varchar certidao_nascimento
        boolean tem_necessidade_especial
    }

    INSCRICOES {
        uuid id PK
        uuid aluno_id FK
        uuid turma_id FK
        varchar protocolo UK
        timestamp data_inscricao
        numeric pontuacao_total
        integer posicao_fila
        varchar status
        timestamp data_chamada
    }

    CRITERIOS {
        uuid id PK
        varchar nome
        numeric pontos_base
        varchar categoria
    }

    INSCRICAO_CRITERIOS {
        uuid id PK
        uuid inscricao_id FK
        uuid criterio_id FK
        numeric pontos_obtidos
    }
```

### Descrição das Entidades:
- **`escolas`**: Representa as unidades escolares com capacidade total de alunos.
- **`turmas`**: Agrupamento por série/ano e turno, controlando a quantidade de vagas totais e disponíveis.
- **`responsaveis`**: Dados cadastrais e renda familiar per capita dos responsáveis legais.
- **`alunos`**: Crianças dependentes vinculadas ao responsável, com certidão de nascimento e necessidades especiais.
- **`criterios`**: Tabela de regras de pontuação ativas (faixa de renda, proximidade, irmãos).
- **`inscricoes`**: Representa a posição do candidato na fila de uma turma, seu protocolo único, status e pontuação.
- **`inscricao_criterios`**: Associação que registra os critérios atendidos e os pontos obtidos em cada inscrição.

---

## 5. Rotas da API (Endpoints REST)

Todas as rotas retornam e recebem cargas úteis no formato `application/json`.

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/escolas` | Lista todas as escolas cadastradas |
| `POST` | `/api/escolas` | Cadastra uma nova escola |
| `GET` | `/api/escolas/{id}/turmas` | Lista as turmas e o saldo de vagas de uma escola |
| `POST` | `/api/turmas` | Cadastra uma nova turma com capacidade inicial de vagas |
| `POST` | `/api/responsaveis` | Cadastra um responsável legal |
| `POST` | `/api/alunos` | Cadastra um aluno vinculado a um responsável |
| `POST` | `/api/inscricoes` | Cria uma solicitação de vaga para o aluno em uma turma |
| `GET` | `/api/inscricoes/protocolo/{protocolo}` | Consulta pública de status e posição na fila por protocolo |
| `GET` | `/api/turmas/{turmaId}/fila` | Retorna a fila de espera da turma ordenada por pontuação |
| `POST` | `/api/turmas/{turmaId}/recalcular` | Dispara o recálculo concorrente da fila via Threads |
| `POST` | `/api/inscricoes/{id}/convocar` | Convoca o candidato para apresentação de documentos |
| `POST` | `/api/inscricoes/{id}/matricular` | Efetiva a matrícula e decrementa vaga de forma atômica |
| `POST` | `/api/inscricoes/{id}/cancelar` | Cancela ou formaliza a desistência da inscrição |

---

## 6. Como Executar o Projeto

### Pré-requisitos
- [Docker](https://www.docker.com/) e Docker Compose instalados.
- [Java JDK 17+](https://adoptium.net/) instalado.

### 1. Iniciar o Banco de Dados (PostgreSQL)
Na raiz do diretório `backend/`, execute:
```bash
docker compose up -d
```
O banco criará a base `sive_db` na porta padrão `5432` e executará automaticamente o script `sql/schema.sql`.

### 2. Executar a Aplicação
Execute o projeto utilizando o Maven Wrapper:
```bash
./mvnw spring-boot:run
```

A API estará disponível em: `http://localhost:8080`
