# SIVE — Sistema Integrado de Vagas Escolares (Backend)

O **SIVE** é uma aplicação voltada para a gestão transparente, organizada e automatizada de solicitações de vagas e filas de espera na educação infantil e ensino fundamental.

O backend expõe uma API REST em **Java 17 LTS + Spring Boot Web** com persistência direta via **JDBC nativo (`java.sql.*`)** em **PostgreSQL 16**, aplicando o padrão **GOF Strategy** para o motor de pontuação, criptografia **AES-256-GCM + HMAC-SHA256** para proteção de CPFs (LGPD) e controle de concorrência com `synchronized` e transações manuais.

---

## 1. Tecnologias Utilizadas

- **Linguagem:** Java 17 LTS
- **Framework Web:** Spring Boot 3.3+ (`spring-boot-starter-web`, `@RestController`, `@RestControllerAdvice`)
- **Banco de Dados:** PostgreSQL 16
- **Acesso a Dados:** JDBC Nativo (`java.sql.*` — `Connection`, `PreparedStatement`, `ResultSet` em `try-with-resources`)
- **Segurança & LGPD:** `TokenService` (token assinado com HMAC-SHA256), `PasswordHasher` (PBKDF2), `CpfCryptoService` (AES-256-GCM + hash determinístico HMAC-SHA256)
- **Testes:** JUnit 5 (`spring-boot-starter-test`)
- **Containerização:** Docker e Docker Compose

---

## 2. Arquitetura e Padrões de Projeto

```text
HTTP Request ──► EscolaAuthInterceptor ──► Controller (REST) ──► Service (Negócio) ──► DAO (JDBC) ──► PostgreSQL
                                                                       │
                                                        ┌──────────────┴──────────────┐
                                                        ▼                             ▼
                                             Strategy (Pontuação)          Tarefa (Threads Concorrentes)
```

### Padrão Strategy + Factory (`com.sive.strategy`)
Toda lógica de pontuação dos alunos na lista de espera é desacoplada por meio do padrão **GOF Strategy**:
- `CriterioPontuacaoStrategy` (interface): define `double calcularPontos(Aluno aluno, Criterio criterio)` e `String getNomeCriterio()`.
- Implementações concretas:
  - `CriterioRendaStrategy`: calcula pontuação escalonada pela renda familiar per capita.
  - `CriterioBooleanoStrategy`: estratégia reutilizável via `Predicate<Aluno>` para critérios binários (`PCD`, `IRMAO`, `VULNERABILIDADE`, `BPC_LOAS`, `BOLSA_FAMILIA`, `RESPONSAVEL_SOLO`, `MORA_ALUGUEL`).
  - `CriterioSemRegraAutomaticaStrategy`: fallback seguro para critérios sem regra automática.
- `CriterioStrategyFactory`: fabrica a estratégia adequada via *Switch Expression* (`obterEstrategia(String codigoCriterio)`).

### Threads e Concorrência (`com.sive.tarefa` e `TurmaService`)
1. **Tarefas Assíncronas (`Runnable`):** `TarefaRecalcularFila`, `TarefaCalcularEstatisticas` e `TarefaNotificarChamada` executam processamentos em threads dedicadas.
2. **Proteção contra *Race Conditions*:** A alocação e decremento de vagas em `TurmaService` utiliza bloqueio `synchronized` combinado com transações JDBC (`conn.setAutoCommit(false)`, `commit()`, `rollback()`).

### Tratamento Centralizado de Exceções (`GlobalExceptionHandler`)
Exceções de domínio (`VagaIndisponivelException`, `AlunoJaInscritoException`, `InscricaoNaoEncontradaException`, `StatusInvalidoException`, `AutenticacaoException`) e `IllegalArgumentException` são interceptadas e convertidas em respostas JSON padronizadas (`400`, `401`, `404`, `409`).

---

## 3. Estrutura de Pastas e Pacotes

```text
backend/
├── Dockerfile                      # Build multi-stage para deploy
├── docker-compose.yml              # Orquestração PostgreSQL 16 + Backend
├── fly.toml                        # Configuração de deploy (Fly.io)
├── pom.xml                         # Dependências Maven (Java 17)
├── client.http                     # Coleção pronta de testes HTTP (VS Code REST Client)
├── diagramas/                      # Diagramas UML em Mermaid
├── database/
│   ├── schema.sql                  # DDL completo
│   ├── migrations/                 # Migrações incrementais (0000 a 0003)
│   └── seeds/                      # Seeds (local.sql com escola EMEI-0001 e dados de teste)
└── src/main/java/com/sive/
    ├── SiveApplication.java
    ├── controller/                 # Controladores REST (Auth, EscolaPainel, ConsultaPublica, Health, etc.)
    ├── service/                    # Regras de negócio e transações
    ├── strategy/                   # Padrão Strategy + Factory (pontuação)
    ├── security/                   # CpfCryptoService, TokenService, PasswordHasher, EscolaAuthInterceptor
    ├── dao/                        # DAOs JDBC puro
    ├── tarefa/                     # Tarefas concorrentes (Runnable)
    ├── model/                      # POJOs e DTOs
    └── exception/                  # Exceções de domínio
```

---

## 4. Rotas da API (Endpoints REST)

Todas as rotas utilizam `application/json`. As rotas sob `/api/escola/**` exigem o cabeçalho `Authorization: Bearer <token>` obtido em `POST /api/auth/login`.

### 4.1 Autenticação e Saúde (Públicas)
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/health` (ou `/health`, `/health-check`) | Verifica se a API e o banco estão operacionais |
| `POST` | `/api/auth/login` | Autentica a escola (`codigo` + `senha`) e retorna o token Bearer |

### 4.2 Consulta Pública — Acompanhar Matrícula (Públicas)
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/publico/consulta` | Consulta solicitações do responsável por `cpf` + `protocolo` |
| `GET` | `/api/publico/consulta/protocolo/{protocolo}` | Consulta direta de uma inscrição específica pelo `protocolo` (link/QR Code) |

### 4.3 Painel da Escola (Autenticadas — `Bearer Token`)
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/escola/perfil` | Retorna dados cadastrais da escola autenticada |
| `GET` | `/api/escola/resumo` | Métricas do dashboard (fila, vagas, turmas, matrículas) |
| `GET` | `/api/escola/turmas` | Lista turmas da escola |
| `POST` | `/api/escola/turmas` | Cria nova turma |
| `GET` | `/api/escola/turmas/{turmaId}` | Detalhes de uma turma |
| `PUT` | `/api/escola/turmas/{turmaId}` | Atualiza uma turma |
| `DELETE` | `/api/escola/turmas/{turmaId}` | Remove uma turma (se não possuir vínculos) |
| `POST` | `/api/escola/turmas/{turmaId}/recalcular` | Dispara recálculo concorrente da fila via Thread |
| `GET` | `/api/escola/responsaveis` | Lista responsáveis (`?busca=` e `?comFilhos=`) com CPF mascarado |
| `GET` | `/api/escola/responsaveis/{responsavelId}` | Detalhes do responsável e filhos (`?incluirCpfCompleto=true` opcional) |
| `POST` | `/api/escola/responsaveis` | Cadastra novo responsável (criptografando CPF) |
| `PUT` | `/api/escola/responsaveis/{responsavelId}` | Atualiza dados do responsável |
| `GET` | `/api/escola/alunos` | Lista alunos da escola (`?busca=` e `?serieAno=`) com CPF mascarado |
| `GET` | `/api/escola/alunos/{alunoId}` | Detalhes do aluno, critérios e responsáveis (`?incluirCpfCompleto=true` opcional) |
| `POST` | `/api/escola/alunos` | Cadastra aluno + vínculos + inscrição na fila (retorna `protocolo`) |
| `PUT` | `/api/escola/alunos/{alunoId}` | Atualiza dados do aluno, critérios, inscrição e responsáveis |
| `GET` | `/api/escola/inscricoes` | Lista inscrições por status (`?status=`, `?busca=`, `?serieAno=`) ordenadas por pontuação |
| `POST` | `/api/escola/inscricoes/{inscricaoId}/chamar` | Convoca aluno da fila (`EM_ESPERA` → `CHAMADO`) |
| `PATCH` | `/api/escola/inscricoes/{inscricaoId}/chamado` | Gerencia chamado (`ACEITAR`, `RECUSAR`, `DESISTIR`, `REPASSAR`, `DESFAZER`) |
| `GET` | `/api/escola/criterios` | Lista critérios de pontuação cadastrados |
| `PUT` | `/api/escola/criterios` | Atualiza pesos/status dos critérios (soma dos ativos = 100) |
| `GET` | `/api/escola/vagas/historico` | Lista histórico de movimentações de vagas |
| `PATCH` | `/api/escola/vagas` | Ajusta manualmente vagas disponíveis de uma turma e registra histórico |

### 4.4 Rotas Base / Administrativas
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/escolas` | Cadastra uma nova escola |
| `POST` | `/api/turmas` / `GET` `/api/turmas/{id}` | Cadastro e consulta direta de turma |
| `GET` | `/api/escolas/{escolaId}/turmas` | Lista turmas por ID da escola |
| `POST` | `/api/responsaveis` / `GET` `/api/responsaveis` / `GET` `/api/responsaveis/{id}` | CRUD base de responsáveis |
| `POST` | `/api/alunos` / `GET` `/api/alunos` / `GET` `/api/alunos/{id}` | CRUD base de alunos |
| `GET` | `/api/alunos/{id}/responsaveis` | Lista responsáveis vinculados a um aluno |

---

## 5. Como Executar e Testar Localmente

### Pré-requisitos
- [Docker](https://www.docker.com/) e Docker Compose
- [Java JDK 17](https://adoptium.net/)

### Subir o Banco e o Backend
```bash
# 1. Subir apenas o PostgreSQL (com schema e seeds automáticos)
cd backend
docker compose up sive-db -d

# 2. Executar a aplicação Spring Boot
./mvnw spring-boot:run   # Linux/macOS
# ou: .\mvnw.cmd spring-boot:run  (Windows)
```
> Para subir tudo em containers (`sive-db` + `sive-backend`): `cd backend && docker compose up --build -d`

### Credenciais padrão (Seed Local)
- **Código da escola:** `EMEI-0001`
- **Senha:** `sive123`
- **Protocolo de exemplo (seed):** `SIVE-SIM0001` (Responsável: Carla Ferreira, CPF `333.444.555-66`)

---

## 6. Instruções de Teste das Rotas (REST Client e cURL)

### Opção 1: Testando com `client.http` no VS Code (Recomendado)
1. Instale a extensão **[REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)** (`humao.rest-client`).
2. Abra o arquivo [`backend/client.http`](./client.http).
3. Execute primeiro a requisição **Login da Escola (`POST /api/auth/login`)** clicando em **Send Request**.
4. Copie o campo `"token"` retornado e cole na variável `@token = ...` no topo do arquivo `client.http`.
5. Clique em **Send Request** em qualquer rota abaixo (resumo, turmas, critérios, alunos, chamados, consulta pública, etc.).

### Opção 2: Testando direto pelo Terminal (`curl`)

#### 1) Health Check
```bash
curl -s http://localhost:8080/api/health
```

#### 2) Login da Escola (obter Token Bearer)
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"codigo":"EMEI-0001","senha":"sive123"}'
```
Copie o valor de `"token"` retornado e exporte no terminal:
```bash
export TOKEN="COLE_O_TOKEN_AQUI"
```

#### 3) Consultar Resumo do Painel e Critérios (Strategy)
```bash
curl -s http://localhost:8080/api/escola/resumo -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/api/escola/criterios -H "Authorization: Bearer $TOKEN"
```

#### 4) Listar Inscrições em Espera e Chamar Próximo da Fila
```bash
curl -s "http://localhost:8080/api/escola/inscricoes?status=EM_ESPERA" \
  -H "Authorization: Bearer $TOKEN"
```

#### 5) Testar Consulta Pública ("Acompanhar Matrícula")
```bash
# Por CPF + Protocolo
curl -s -X POST http://localhost:8080/api/publico/consulta \
  -H "Content-Type: application/json" \
  -d '{"cpf":"333.444.555-66","protocolo":"SIVE-SIM0001"}'

# Direto pelo Protocolo (Link / QR Code)
curl -s http://localhost:8080/api/publico/consulta/protocolo/SIVE-SIM0001
```

### Executar Testes Automatizados (JUnit 5)
```bash
cd backend && ./mvnw test
```
Os testes cobrem: `CriterioPontuacaoTest` (Padrão Strategy), `CpfCryptoServiceTest` (AES-256-GCM + HMAC + mascaramento), `TokenServiceTest`, `PasswordHasherTest`, `AuthServiceTest` e `SiveApplicationTests`.
