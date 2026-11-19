# Diagrama de Casos de Uso — SIVE

O Diagrama de Casos de Uso descreve as interações com o **SIVE (Sistema Integrado de Vagas Escolares)**, detalhando todos os atores envolvidos: atores primários (usuários humanos), ator de interface (frontend) e atores secundários/suporte (threads de background e banco de dados), além dos relacionamentos formais de **`<<include>>`** e **`<<extend>>`**.

---

## Atores do Sistema

### Atores Primários (Usuários Humanos)
- **Responsável Legal:** Cidadão que cadastra seu perfil, registra os dependentes legais, formaliza solicitações de vagas na escola desejada, consulta o protocolo e pode solicitar o cancelamento.
- **Gestor Escolar / Administrador:** Agente público que cadastra as unidades escolares e turmas, monitora as filas de espera, convoca candidatos e formaliza a efetivação da matrícula.

### Ator de Interface
- **Frontend (Interface Web / SPA):** Aplicação cliente que captura os dados digitados pelos usuários, valida formulários em tempo de tela, formata payloads JSON e consome as APIs REST do backend.

### Atores Secundários / Suporte (Técnicos e Infraestrutura)
- **Processador em Background (Threads / Jobs SIVE):** Subsistema concorrente que executa tarefas demoradas e assíncronas (`Runnable`), como reordenação em lote da fila e simulação de notificações.
- **Banco de Dados (PostgreSQL 16):** Sistema gerenciador de banco de dados relacional que atua como ator de suporte na persistência das entidades, garantia de constraints de integridade e locks transacionais.

---

## Diagrama (Mermaid)

```mermaid
flowchart LR
    %% Atores Primários (Humanos)
    subgraph AtoresHumanos["Atores Primários (Usuários)"]
        Resp["fa:fa-user Responsável Legal"]
        Admin["fa:fa-user-tie Gestor Escolar"]
    end

    %% Ator de Intermediação de Interface
    subgraph Intermediario["Ator de Interface"]
        Front["fa:fa-desktop Frontend (SPA / Web)"]
    end

    %% Fronteira do Sistema Backend
    subgraph CasosDeUso["Sistema SIVE (Backend REST)"]
        UC01(["Cadastrar Escola"])
        UC02(["Cadastrar Turma e Vagas"])
        UC03(["Cadastrar Responsável Legal"])
        UC04(["Cadastrar Aluno Vinculado"])
        UC05(["Solicitar Vaga (Criar Inscrição)"])
        UC06(["Calcular Pontuação Socioeconômica (Strategy)"])
        UC07(["Consultar Fila por Protocolo"])
        UC08(["Visualizar Fila da Turma"])
        UC09(["Recalcular Classificação da Fila"])
        UC10(["Convocar Candidato"])
        UC11(["Efetivar Matrícula (Atômica / Synchronized)"])
        UC12(["Cancelar Inscrição"])
        UC13(["Validar Vínculo Aluno-Responsável"])
        UC14(["Validar Disponibilidade de Vaga"])
        UC15(["Enviar Notificação de Convocação"])
        UC16(["Registrar Justificativa de Desistência"])
    end

    %% Atores Secundários / Infraestrutura
    subgraph AtoresSuporte["Atores Secundários (Suporte / Infraestrutura)"]
        ThreadBatch["fa:fa-cogs Processador em Background\n(Threads/Jobs)"]
        DB[("fa:fa-database Banco de Dados\n(PostgreSQL 16)")]
    end

    %% Interação Humanos com Frontend
    Resp -->|Utiliza| Front
    Admin -->|Utiliza| Front

    %% Frontend disparando Casos de Uso REST
    Front -->|POST /api/responsaveis| UC03
    Front -->|POST /api/alunos| UC04
    Front -->|POST /api/inscricoes| UC05
    Front -->|GET /api/inscricoes/protocolo| UC07
    Front -->|POST /api/inscricoes/.../cancelar| UC12

    Front -->|POST /api/escolas| UC01
    Front -->|POST /api/turmas| UC02
    Front -->|GET /api/turmas/.../fila| UC08
    Front -->|POST /api/turmas/.../recalcular| UC09
    Front -->|POST /api/inscricoes/.../convocar| UC10
    Front -->|POST /api/inscricoes/.../matricular| UC11

    %% Relacionamentos <<include>> (Obrigatórios)
    UC05 -.->|"<<include>>"| UC06
    UC05 -.->|"<<include>>"| UC13
    UC11 -.->|"<<include>>"| UC14

    %% Relacionamentos <<extend>> (Opcionais / Condicionais)
    UC15 -.->|"<<extend>>\n(Se contato cadastrado)"| UC10
    UC09 -.->|"<<extend>>\n(Após nova inscrição)"| UC05
    UC16 -.->|"<<extend>>\n(Se motivo informado)"| UC12

    %% Interação com Threads em Background
    UC09 -.->|dispara thread assíncrona| ThreadBatch
    UC15 -.->|executado via thread| ThreadBatch

    %% Interação com Banco de Dados (Persistência e Leitura)
    UC01 -->|persiste| DB
    UC02 -->|persiste| DB
    UC03 -->|persiste| DB
    UC04 -->|persiste| DB
    UC05 -->|persiste| DB
    UC07 -->|consulta| DB
    UC08 -->|consulta| DB
    UC09 -->|atualiza posições| DB
    UC10 -->|atualiza status| DB
    UC11 -->|decrementa vaga e atualiza status| DB
    UC12 -->|atualiza status| DB
    ThreadBatch -->|lê e atualiza em lote| DB
```

---

## Especificação dos Relacionamentos `<<include>>` e `<<extend>>`

### 1. Relacionamentos de Inclusão (`<<include>>`) — Obrigatórios
São etapas essenciais que sempre são executadas como parte do caso de uso base:

- **UC05 (Solicitar Vaga) `<<include>>` UC06 (Calcular Pontuação Socioeconômica):**
  - Toda nova inscrição precisa obrigatoriamente executar o motor de cálculo polimórfico (`Strategy`) para definir sua pontuação inicial.
- **UC05 (Solicitar Vaga) `<<include>>` UC13 (Validar Vínculo Aluno-Responsável):**
  - Toda inscrição exige a verificação obrigatória da existência do aluno e do vínculo prévio com o responsável solicitante.
- **UC11 (Efetivar Matrícula) `<<include>>` UC14 (Validar Disponibilidade de Vaga):**
  - A efetivação da matrícula exige obrigatoriamente a verificação sincronizada (`synchronized`) do saldo de vagas (`vagas_disponiveis > 0`) antes de decrementar e mudar o status para `MATRICULADO`.

### 2. Relacionamentos de Extensão (`<<extend>>`) — Condicionais / Opcionais
São comportamentos opcionais que estendem o caso de uso base mediante uma condição específica ou ponto de extensão:

- **UC15 (Enviar Notificação de Convocação) `<<extend>>` UC10 (Convocar Candidato):**
  - *Ponto de extensão:* Pós-convocação.
  - *Condição:* Disparado assincronamente por thread em segundo plano caso o responsável possua telefone/contato válido cadastrado.
- **UC09 (Recalcular Classificação da Fila) `<<extend>>` UC05 (Solicitar Vaga):**
  - *Ponto de extensão:* Pós-criação da inscrição.
  - *Condição:* Disparado em background (`TarefaRecalcularFila`) para reordenar todas as posições da fila após a inserção de um novo candidato.
- **UC16 (Registrar Justificativa de Desistência) `<<extend>>` UC12 (Cancelar Inscrição):**
  - *Ponto de extensão:* Ao formalizar desistência.
  - *Condição:* Executado opcionalmente quando o responsável ou gestor preenche a motivação do cancelamento.

---

## Matriz de Rastreabilidade de Casos de Uso e Atores

| ID | Caso de Uso | Ator Primário | Ator de Interface | Atores Secundários / Relação | Descrição |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **UC01** | Cadastrar Escola | Gestor Escolar | Frontend | Banco de Dados | Registra novas escolas e suas capacidades no banco. |
| **UC02** | Cadastrar Turma e Vagas | Gestor Escolar | Frontend | Banco de Dados | Cadastra turmas vinculadas às escolas com número de vagas por turno. |
| **UC03** | Cadastrar Responsável Legal | Responsável Legal | Frontend | Banco de Dados | Registra dados pessoais, renda per capita e telefone do responsável. |
| **UC04** | Cadastrar Aluno Vinculado | Responsável Legal | Frontend | Banco de Dados | Salva dados da certidão, deficiência e o vínculo N:N com responsáveis. |
| **UC05** | Solicitar Vaga (Inscrição) | Responsável Legal | Frontend | Background, Banco de Dados <br>• *includes* UC06, UC13 <br>• *extended by* UC09 | Cria a inscrição, calcula pontos (`Strategy`), persiste e aciona a reclassificação. |
| **UC06** | Calcular Pontuação Socioeconômica | *Sistema interno* | — | Banco de Dados | Avalia renda, proximidade, irmãos e PCD para somar a pontuação. |
| **UC07** | Consultar Fila por Protocolo | Responsável Legal | Frontend | Banco de Dados | Consulta pública da posição do candidato sem expor dados sensíveis. |
| **UC08** | Visualizar Fila da Turma | Gestor Escolar | Frontend | Banco de Dados | Lista os inscritos da turma ordenados por pontuação decrescente. |
| **UC09** | Recalcular Classificação | Gestor Escolar | Frontend | Background, Banco de Dados | Dispara rotina concorrente via Thread para reordenar toda a fila. |
| **UC10** | Convocar Candidato | Gestor Escolar | Frontend | Background, Banco de Dados <br>• *extended by* UC15 | Altera o status para `CHAMADO` e dispara simulação de notificação. |
| **UC11** | Efetivar Matrícula | Gestor Escolar | Frontend | Banco de Dados <br>• *includes* UC14 | Decrementa vaga de forma atômica/sincronizada e fixa o status `MATRICULADO`. |
| **UC12** | Cancelar Inscrição | Responsável / Gestor | Frontend | Banco de Dados <br>• *extended by* UC16 | Formaliza a desistência ou anulação da inscrição na fila. |
| **UC13** | Validar Vínculo Aluno-Responsável | *Sistema interno* | — | Banco de Dados | Valida se a criança informada possui responsável legal associado. |
| **UC14** | Validar Disponibilidade de Vaga | *Sistema interno* | — | Banco de Dados | Verifica de forma sincronizada se o saldo de vagas é maior que zero. |
| **UC15** | Enviar Notificação de Convocação | *Sistema interno* | — | Background | Envia notificação por tarefa assíncrona ao responsável convocado. |
| **UC16** | Registrar Justificativa | Responsável / Gestor | Frontend | Banco de Dados | Armazena o motivo formal da desistência ou cancelamento. |
