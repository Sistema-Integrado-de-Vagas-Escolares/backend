# Diagrama de Casos de Uso — SIVE

O Diagrama de Casos de Uso descreve os atores e suas interações com as funcionalidades do **SIVE (Sistema Integrado de Vagas Escolares)**, atualizado para contemplar autenticação da escola por código/senha, painel gestor da escola, consulta pública com CPF e protocolo, e criptografia/hashing para conformidade com a LGPD.

---

## Atores do Sistema

### Atores Primários (Usuários Humanos)
- **Cidadão / Responsável Legal:** Realiza o cadastro familiar (aluno e responsáveis com criptografia de CPF), seleciona a turma e solicita a vaga, consulta o status e posição ordinal por meio de consulta pública aberta e pode solicitar o cancelamento.
- **Gestor Escolar / Equipe da Escola:** Autentica-se no portal administrativo com código institucional e senha (`/api/auth/login`), visualiza o painel de turmas e ocupação, atualiza saldo de vagas com histórico auditado, convoca candidatos da fila de espera e efetiva matrículas.

### Ator de Interface
- **Frontend (Web / SPA):** Interface do usuário que valida formulários, armazena token de autenticação (Bearer Token) para rotas protegidas da escola e consome os endpoints REST.

### Atores Secundários / Suporte (Infraestrutura)
- **Processador em Background (Threads / Jobs SIVE):** Subsistema concorrente multithread (`Runnable`) para reordenar filas (`TarefaRecalcularFila`), agregar métricas da rede (`TarefaCalcularEstatisticas`) e disparar simulação de avisos de convocação (`TarefaNotificarChamada`).
- **Banco de Dados (PostgreSQL 16):** Banco relacional com 9 tabelas (`escolas`, `turmas`, `responsaveis`, `alunos`, `aluno_responsaveis`, `criterios`, `inscricoes`, `inscricao_criterios`, `historico_vagas`) com controle transacional e integridade referencial.

---

## Diagrama (Mermaid)

```mermaid
flowchart LR
    %% Atores Primários
    subgraph AtoresHumanos["Atores Primários"]
        Resp["fa:fa-user Cidadão / Responsável"]
        Gestor["fa:fa-user-tie Gestor Escolar"]
    end

    %% Ator de Interface
    subgraph Intermediario["Ator de Interface"]
        Front["fa:fa-desktop Frontend (SPA / Web)"]
    end

    %% Casos de Uso Backend
    subgraph CasosDeUso["Sistema SIVE (Backend REST)"]
        %% Gestão e Autenticação
        UC_Auth(["Autenticar Escola (Código/Senha)"])
        UC_Painel(["Consultar Painel da Escola (Dashboard)"])
        UC_Vagas(["Atualizar Saldo de Vagas da Turma"])
        UC_HistVagas(["Registrar Histórico de Alteração de Vagas"])

        %% Cidadão / Família
        UC_CadResp(["Cadastrar Responsável Legal"])
        UC_CadAluno(["Cadastrar Aluno e Endereço"])
        UC_Cripto(["Criptografar CPF e Gerar Hash (LGPD)"])
        UC_Inscricao(["Solicitar Vaga (Criar Inscrição)"])
        UC_CalcPontos(["Calcular Pontuação Socioeconômica (Strategy)"])
        UC_ConsultaPublica(["Consultar Status por Protocolo/CPF"])

        %% Gestão da Fila
        UC_FilaTurma(["Visualizar Fila Ordenada da Turma"])
        UC_Recalcular(["Recalcular Classificação Concorrente"])
        UC_Convocar(["Convocar Candidato da Fila"])
        UC_Notificar(["Enviar Notificação de Chamada"])
        UC_Matricular(["Efetivar Matrícula (Atomicamente)"])
        UC_Cancelar(["Cancelar Inscrição"])
    end

    %% Atores Secundários
    subgraph AtoresSuporte["Atores de Suporte"]
        ThreadBatch["fa:fa-cogs Processador em Background\n(Threads/Runnable)"]
        DB[("fa:fa-database PostgreSQL 16\n(sive_db)")]
    end

    %% Interação Humanos -> Frontend
    Resp -->|Utiliza| Front
    Gestor -->|Utiliza| Front

    %% Disparo de Casos de Uso pelo Frontend
    Front -->|POST /api/auth/login| UC_Auth
    Front -->|GET /api/escola-painel/resumo| UC_Painel
    Front -->|PUT /api/turmas/.../vagas| UC_Vagas
    Front -->|POST /api/responsaveis| UC_CadResp
    Front -->|POST /api/alunos| UC_CadAluno
    Front -->|POST /api/inscricoes| UC_Inscricao
    Front -->|POST /api/consulta-publica| UC_ConsultaPublica
    Front -->|GET /api/turmas/.../fila| UC_FilaTurma
    Front -->|POST /api/turmas/.../recalcular| UC_Recalcular
    Front -->|POST /api/escola-painel/inscricoes/.../gerenciar| UC_Convocar
    Front -->|POST /api/inscricoes/.../matricular| UC_Matricular
    Front -->|POST /api/inscricoes/.../cancelar| UC_Cancelar

    %% Relacionamentos <<include>>
    UC_CadResp -.->|"<<include>>"| UC_Cripto
    UC_CadAluno -.->|"<<include>>"| UC_Cripto
    UC_Inscricao -.->|"<<include>>"| UC_CalcPontos
    UC_Vagas -.->|"<<include>>"| UC_HistVagas

    %% Relacionamentos <<extend>>
    UC_Recalcular -.->|"<<extend>>\n(Após nova inscrição)"| UC_Inscricao
    UC_Notificar -.->|"<<extend>>\n(Se contato preenchido)"| UC_Convocar

    %% Interações com Threads de Background
    UC_Recalcular -.->|executa em Thread| ThreadBatch
    UC_Notificar -.->|executa em Thread| ThreadBatch

    %% Persistência no Banco de Dados
    UC_Auth -->|valida credenciais| DB
    UC_Painel -->|agrega dados| DB
    UC_CadResp -->|persiste| DB
    UC_CadAluno -->|persiste| DB
    UC_Inscricao -->|persiste| DB
    UC_ConsultaPublica -->|consulta por hash/protocolo| DB
    UC_Vagas -->|atualiza vagas| DB
    UC_HistVagas -->|insere log histórico| DB
    UC_Convocar -->|atualiza status| DB
    UC_Matricular -->|decrementa vaga e atualiza status| DB
    UC_Cancelar -->|atualiza status| DB
    ThreadBatch -->|atualiza posições em lote| DB
```

---

## Matriz de Rastreabilidade Atualizada

| ID | Caso de Uso | Ator Primário | Ator de Interface | Atores Secundários / Relação | Descrição |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **UC01** | Autenticar Escola | Gestor Escolar | Frontend | Banco de Dados | Valida código e senha (BCrypt) e emite Bearer Token temporário. |
| **UC02** | Consultar Painel da Escola | Gestor Escolar | Frontend | Banco de Dados | Exibe dashboard da escola com totais de vagas, inscritos e chamados. |
| **UC03** | Atualizar Saldo de Vagas | Gestor Escolar | Frontend | Banco de Dados <br>• *includes* UC_HistVagas | Ajusta vagas totais e disponíveis da turma registrando auditoria. |
| **UC04** | Cadastrar Responsável Legal | Responsável Legal | Frontend | Banco de Dados <br>• *includes* UC_Cripto | Salva dados com CPF criptografado bidirecional e hash SHA-256 para busca. |
| **UC05** | Cadastrar Aluno e Endereço | Responsável Legal | Frontend | Banco de Dados <br>• *includes* UC_Cripto | Registra dependente, dados de moradia, renda per capita e vínculo familiar. |
| **UC06** | Solicitar Vaga (Inscrição) | Responsável Legal | Frontend | Banco de Dados, Background <br>• *includes* UC_CalcPontos <br>• *extended by* UC_Recalcular | Inscreve candidato na turma, calcula critérios socioeconômicos e gera protocolo. |
| **UC07** | Calcular Pontuação Socioeconômica | *Sistema interno* | — | Banco de Dados | Avalia renda, vulnerabilidade, solo, aluguel, Bolsa Família, BPC e irmão via Strategy. |
| **UC08** | Consulta Pública de Fila | Responsável Legal | Frontend | Banco de Dados | Consulta posição ordinal e status por protocolo ou CPF do aluno/responsável. |
| **UC09** | Recalcular Classificação Concorrente | Gestor Escolar | Frontend | Background, Banco de Dados | Dispara thread assíncrona (`TarefaRecalcularFila`) para reordenar candidatos. |
| **UC10** | Convocar Candidato | Gestor Escolar | Frontend | Banco de Dados, Background <br>• *extended by* UC_Notificar | Altera status para `CHAMADO` e dispara simulação de notificação via thread. |
| **UC11** | Efetivar Matrícula | Gestor Escolar | Frontend | Banco de Dados | Aloca a vaga com proteção atômica (`synchronized`), decrementando saldo da turma. |
| **UC12** | Cancelar Inscrição | Responsável / Gestor | Frontend | Banco de Dados | Registra desistência formal ou cancelamento da solicitação. |
