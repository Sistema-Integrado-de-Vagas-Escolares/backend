# Diagrama de Classes — SIVE

O Diagrama de Classes representa a estrutura orientada a objetos do domínio do backend do **SIVE**, incluindo a hierarquia de herança (`Pessoa`), associações, enums, contratos do padrão **Strategy** e as tarefas concorrentes.

---

## Diagrama (Mermaid)

```mermaid
classDiagram
    %% Herança de Pessoa
    class Pessoa {
        <<abstract>>
        #UUID id
        #String nome
        +getId() UUID
        +getNome() String
        +setNome(String nome) void
    }

    class Responsavel {
        -String cpf
        -String telefone
        -BigDecimal rendaFamiliar
        +getCpf() String
        +getTelefone() String
        +getRendaFamiliar() BigDecimal
    }

    class Aluno {
        -LocalDate dataNascimento
        -String certidaoNascimento
        -boolean temNecessidadeEspecial
        +getDataNascimento() LocalDate
        +getCertidaoNascimento() String
        +isTemNecessidadeEspecial() boolean
    }

    Pessoa <|-- Responsavel
    Pessoa <|-- Aluno

    %% Associação N:N Aluno e Responsavel
    class AlunoResponsavel {
        -UUID id
        -UUID alunoId
        -UUID responsavelId
        -String parentesco
        -boolean isContatoPrincipal
        +getParentesco() String
        +isContatoPrincipal() boolean
    }

    Aluno "1" -- "0..*" AlunoResponsavel : possui
    Responsavel "1" -- "0..*" AlunoResponsavel : vincula-se

    %% Estrutura Escolar
    class Escola {
        -UUID id
        -String nome
        -String bairro
        -String cidade
        -Integer capacidadeTotal
        +getId() UUID
        +getNome() String
        +getCapacidadeTotal() Integer
    }

    class Turma {
        -UUID id
        -UUID escolaId
        -String serieAno
        -Turno turno
        -Integer vagasTotais
        -Integer vagasDisponiveis
        +getId() UUID
        +getTurno() Turno
        +getVagasDisponiveis() Integer
        +decrementarVaga() boolean
    }

    class Turno {
        <<enumeration>>
        MANHA
        TARDE
        INTEGRAL
    }

    Escola "1" *-- "0..*" Turma : oferta
    Turma --> Turno : possui

    %% Inscrições e Status
    class StatusInscricao {
        <<enumeration>>
        EM_ESPERA
        CHAMADO
        MATRICULADO
        CANCELADO
        EXPIRADO
    }

    class Inscricao {
        -UUID id
        -UUID alunoId
        -UUID turmaId
        -String protocolo
        -LocalDateTime dataInscricao
        -Double pontuacaoTotal
        -StatusInscricao status
        -LocalDateTime dataChamada
        +calcularPontuacao(List~CriterioPontuacaoStrategy~ strategies) void
        +getStatus() StatusInscricao
        +setStatus(StatusInscricao status) void
    }

    class InscricaoCriterio {
        -UUID id
        -UUID inscricaoId
        -UUID criterioId
        -Double pontosObtidos
    }

    Aluno "1" -- "0..*" Inscricao : candidata-se
    Turma "1" -- "0..*" Inscricao : recebe
    Inscricao --> StatusInscricao : possui
    Inscricao "1" *-- "0..*" InscricaoCriterio : detalha

    %% Padrão Strategy
    class CriterioPontuacaoStrategy {
        <<interface>>
        +calcularPontos(Inscricao inscricao)* double
    }

    class CriterioRendaStrategy {
        +calcularPontos(Inscricao inscricao) double
    }

    class CriterioProximidadeStrategy {
        +calcularPontos(Inscricao inscricao) double
    }

    class CriterioIrmaoStrategy {
        +calcularPontos(Inscricao inscricao) double
    }

    class CriterioPcdStrategy {
        +calcularPontos(Inscricao inscricao) double
    }

    class CriterioStrategyFactory {
        +obterEstrategia(String categoria) CriterioPontuacaoStrategy
    }

    CriterioPontuacaoStrategy <|.. CriterioRendaStrategy
    CriterioPontuacaoStrategy <|.. CriterioProximidadeStrategy
    CriterioPontuacaoStrategy <|.. CriterioIrmaoStrategy
    CriterioPontuacaoStrategy <|.. CriterioPcdStrategy
    CriterioStrategyFactory ..> CriterioPontuacaoStrategy : fabrica

    %% Concorrência e Threads
    class Runnable {
        <<interface>>
        +run()* void
    }

    class TarefaRecalcularFila {
        -UUID turmaId
        +run() void
    }

    class TarefaCalcularEstatisticas {
        +run() void
    }

    class TarefaNotificarChamada {
        -UUID inscricaoId
        +run() void
    }

    Runnable <|.. TarefaRecalcularFila
    Runnable <|.. TarefaCalcularEstatisticas
    Runnable <|.. TarefaNotificarChamada
```

---

## Destaques da Modelagem Orientada a Objetos
1. **Herança e Reuso:** `Pessoa` atua como classe base abstrata para `Aluno` e `Responsavel`, compartilhando identificação e atributos comuns.
2. **Encapsulamento e Polimorfismo:** `CriterioPontuacaoStrategy` permite que novos critérios de pontuação socioeconômica sejam plugados sem modificar o fluxo de cálculo da inscrição (Princípio Aberto/Fechado).
3. **Desacoplamento por Concorrência:** As classes do pacote `tarefa` implementam `Runnable` para permitir execução assíncrona gerenciada por threads dedicadas.

