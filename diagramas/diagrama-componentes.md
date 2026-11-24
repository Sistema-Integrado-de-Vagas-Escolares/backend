# Diagrama de Componentes — SIVE

O Diagrama de Componentes apresenta a divisão modular do sistema **SIVE**, incluindo os portais de consumo, módulos de autenticação/segurança, processamento assíncrono e banco de dados relacional.

---

## Diagrama (Mermaid)

```mermaid
flowchart TD
    subgraph Consumidores["Clientes / Interfaces do Usuário"]
        PortalPublico["Portal do Cidadão (Web/SPA)\n- Cadastro e Inscrição\n- Consulta Pública"]
        PainelEscola["Painel Administrativo da Escola\n- Login e Gestão de Vagas\n- Convocação e Matrícula"]
    end

    subgraph BackendApp["SIVE Backend (Spring Boot 3.3.5 / Java 17)"]
        subgraph ModuloSeguranca["Componente de Segurança & Interceptação"]
            AuthInterceptor["EscolaAuthInterceptor\n(Bearer Token / Header)"]
            CryptoModule["CpfCryptoService & PasswordHasher\n(AES-GCM / SHA-256 / BCrypt)"]
            TokenModule["TokenService"]
        end

        subgraph ModuloREST["Componente de Entrada (REST API)"]
            AuthController["AuthController (/api/auth)"]
            EscolaPainelCtrl["EscolaPainelController (/api/escola-painel)"]
            ConsultaCtrl["ConsultaPublicaController (/api/consulta-publica)"]
            EscolaCtrl["EscolaController (/api/escolas)"]
            TurmaCtrl["TurmaController (/api/turmas)"]
            AlunoCtrl["AlunoController (/api/alunos)"]
            GlobalHandler["GlobalExceptionHandler (@RestControllerAdvice)"]
        end

        subgraph ModuloNegocio["Componente de Serviços & Regras de Negócio"]
            AuthService["AuthService"]
            InscricaoService["InscricaoService"]
            TurmaService["TurmaService (synchronized alocarVaga)"]
            HistoricoService["HistoricoVagaService"]
            ConsultaService["ConsultaPublicaService"]
            StrategyEngine["Motor Strategy\n(CriterioStrategyFactory, CriterioRenda, CriterioBooleano)"]
        end

        subgraph ModuloConcorrencia["Componente de Concorrência & Background"]
            RecalcularTask["TarefaRecalcularFila (Runnable)"]
            NotificarTask["TarefaNotificarChamada (Runnable)"]
            EstatisticasTask["TarefaCalcularEstatisticas (Runnable)"]
        end

        subgraph ModuloPersistencia["Componente de Acesso a Dados (JDBC Nativo)"]
            DAOs["DAOs JDBC\n(AlunoDAO, ResponsavelDAO, TurmaDao, InscricaoDAO,\nEscolaDAO, ConsultaPublicaDAO, HistoricoVagaDAO, CriterioDAO)"]
            ConexaoManager["Gerenciador de Conexão JDBC\n(com.sive.dao.Conexao)"]
        end
    end

    subgraph Infraestrutura["Infraestrutura de Dados"]
        PostgresDB[("PostgreSQL 16 (sive_db)\n- 9 Tabelas Normalizadas\n- Índices e Constraints")]
    end

    %% Ligações dos Clientes
    PortalPublico -->|HTTP REST JSON| ModuloREST
    PainelEscola -->|HTTP REST JSON com Bearer Token| AuthInterceptor
    AuthInterceptor --> ModuloREST

    %% Segurança no fluxo de entrada
    AuthController --> AuthService
    AuthService --> TokenModule
    AuthService --> CryptoModule
    ModuloREST --> GlobalHandler

    %% REST para Serviços
    ModuloREST --> ModuloNegocio
    ModuloNegocio --> StrategyEngine
    ModuloNegocio --> ModuloConcorrencia

    %% Serviços e Tarefas para DAOs
    ModuloNegocio --> ModuloPersistencia
    ModuloConcorrencia --> ModuloPersistencia
    DAOs --> CryptoModule
    DAOs --> ConexaoManager

    %% Conexão ao Banco
    ConexaoManager -->|Driver PostgreSQL JDBC 42.7.4| PostgresDB
```

---

## Descrição dos Módulos Principais

- **Componente de Segurança & Interceptação:**
  - `EscolaAuthInterceptor`: intercepta e protege endpoints administrativos exigindo token válido (`Authorization: Bearer <token>`).
  - `CpfCryptoService`: cifra e decifra CPFs com AES e gera hashes determinísticos em SHA-256 para buscas diretas sem expor dados pessoais (LGPD).
  - `PasswordHasher`: valida senhas criptografadas com salt via algoritmo BCrypt.
- **Componente de Entrada (REST API):** Endpoints segmentados por domínio funcional (autenticação, painel gestor, consulta pública com CAPTCHA/busca segura, CRUD de escolas e turmas).
- **Componente de Concorrência:** Isola rotas pesadas em threads em background com sincronização por `join()` e execução assíncrona.
- **Componente de Acesso a Dados (JDBC Nativo):** Transações manuais via `java.sql` garantindo consistência estrita sem abstrações pesadas de ORM.
