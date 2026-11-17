# Diagrama de Componentes — SIVE

O Diagrama de Componentes apresenta a divisão de blocos executáveis e subsistemas de software do **SIVE**, ilustrando a comunicação entre a camada externa de consumo HTTP, o servidor Spring Boot e a persistência de dados.

---

## Diagrama (Mermaid)

```mermaid
flowchart TD
    subgraph Clientes["Clientes & Consumidores"]
        WebBrowser["Navegador Web / Portal do Cidadão"]
        AdminPortal["Painel Administrativo da Educação"]
    end

    subgraph BackendApp["SIVE Backend (Spring Boot 3.3.5 / Java 17)"]
        subgraph ModuloREST["Componente de Entrada (REST API)"]
            Controllers["REST Controllers\n(Escola, Turma, Responsavel, Aluno, Inscricao)"]
            GlobalHandler["GlobalExceptionHandler\n(@RestControllerAdvice)"]
        end

        subgraph ModuloServicos["Componente de Domínio & Regras de Negócio"]
            Services["Services\n(InscricaoService, TurmaService, EscolaService, etc.)"]
            StrategyEngine["Motor de Pontuação Strategy\n(CriterioStrategyFactory & Estratégias)"]
        end

        subgraph ModuloConcorrencia["Componente de Tarefas & Threads"]
            AsyncTasks["Tarefas Assíncronas (Runnable)\n(RecalcularFila, Notificacoes, Estatisticas)"]
        end

        subgraph ModuloAcessoDados["Componente de Acesso a Dados (JDBC Nativo)"]
            DAOs["DAOs JDBC\n(EscolaDAO, TurmaDAO, AlunoDAO, InscricaoDAO, etc.)"]
            ConnectionPool["Gerenciador de Conexão\n(com.sive.dao.Conexao)"]
        end
    end

    subgraph Infraestrutura["Infraestrutura de Persistência"]
        PostgresDB[("PostgreSQL 16\n(sive_db em Docker)")]
    end

    %% Ligações entre componentes
    WebBrowser -->|JSON / HTTP REST| Controllers
    AdminPortal -->|JSON / HTTP REST| Controllers
    Controllers --> GlobalHandler

    Controllers --> Services
    Services --> StrategyEngine
    Services --> AsyncTasks
    Services --> DAOs
    AsyncTasks --> DAOs

    DAOs --> ConnectionPool
    ConnectionPool -->|Driver JDBC PostgreSQL 42.7.4| PostgresDB
```

---

## Detalhamento dos Componentes

- **Componente de Entrada (REST API):** Ponto de terminação HTTP. Controladores recebem e validam DTOs e cargas úteis JSON, enquanto o `GlobalExceptionHandler` unifica os erros 400, 404, 409 e 500.
- **Componente de Domínio & Regras de Negócio:** Contém os serviços que coordenam o ciclo de vida das inscrições, a sincronização do saldo de vagas (`synchronized`) e a invocação polimórfica de estratégias de pontuação.
- **Componente de Tarefas & Threads:** Isola tarefas demoradas e intensivas de I/O em threads em background com sincronização por `start()` e `join()`.
- **Componente de Acesso a Dados:** Utiliza JDBC nativo via `java.sql` com transações manuais para evitar *overhead* de ORM e garantir controle atômico rigoroso nas operações de reserva de vagas.
- **PostgreSQL 16:** Banco de dados relacional orquestrado via Docker Compose, com constraints de integridade referencial e chaves estrangeiras.

