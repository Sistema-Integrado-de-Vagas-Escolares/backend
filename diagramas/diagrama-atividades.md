# Diagrama de Atividades — SIVE

O Diagrama de Atividades descreve o fluxo de processo de **Solicitação de Vaga e Processamento da Inscrição**, desde o envio inicial pelo responsável até a inclusão ordenada na fila de espera.

---

## Diagrama (Mermaid)

```mermaid
flowchart TD
    Inicio([Início: Responsável solicita vaga]) --> ReceberPayload["Receber dados do Aluno e Turma no Controller"]
    ReceberPayload --> ValidarCadastro{"Aluno e Responsável cadastrados?"}

    ValidarCadastro -- Não --> RetornarErroCadastro["Retornar HTTP 400: Dados inválidos/incompletos"] --> FimErro([Fim com Erro])
    ValidarCadastro -- Sim --> ChecarDuplicidade{"Aluno já possui inscrição ativa na turma?"}

    ChecarDuplicidade -- Sim --> LancarAlunoInscrito["Lançar AlunoJaInscritoException (HTTP 409)"] --> FimErro
    ChecarDuplicidade -- Não --> GerarProtocolo["Gerar código único de protocolo"]

    GerarProtocolo --> BuscarCriterios["Consultar critérios ativos no banco"]
    BuscarCriterios --> IniciarCalculoPontos["Iterar critérios via CriterioStrategyFactory"]

    subgraph CalculoStrategy["Motor de Pontuação Strategy"]
        direction TB
        Criterio1["Calcular CriterioRendaStrategy"]
        Criterio2["Calcular CriterioProximidadeStrategy"]
        Criterio3["Calcular CriterioIrmaoStrategy"]
        Criterio4["Calcular CriterioPcdStrategy"]
        Somar["Totalizar pontos obtidos"]
        Criterio1 --> Somar
        Criterio2 --> Somar
        Criterio3 --> Somar
        Criterio4 --> Somar
    end

    IniciarCalculoPontos --> CalculoStrategy
    CalculoStrategy --> DefinirStatus["Definir Status = EM_ESPERA"]

    DefinirStatus --> SalvarInscricao["Persistir Inscrição e Detalhes no BD (InscricaoDAO)"]
    SalvarInscricao --> DispararThread["Disparar TarefaRecalcularFila em Thread separada"]

    subgraph ThreadAsync["Thread Concorrente em Background"]
        OrdenarFila["Recuperar todas as inscrições EM_ESPERA da turma"]
        OrdenarFila --> OrdenarDecrescente["Ordenar por pontuação DESC e data ASC"]
        OrdenarDecrescente --> AtualizarPosicoes["Atualizar posicao_fila de cada candidato no BD"]
    end

    DispararThread -.-> ThreadAsync
    DispararThread --> RetornarSucesso["Retornar HTTP 201 com Protocolo e Pontuação"]
    RetornarSucesso --> FimSucesso([Fim com Sucesso])
```

---

## Principais Decisões e Condições do Fluxo

1. **Validação Preliminar:** Garante que a criança e seus responsáveis legais estejam previamente registrados na base e devidamente associados.
2. **Prevenção de Duplicidade:** Evita que o mesmo aluno tenha duas inscrições ativas concorrentes para a mesma turma.
3. **Cálculo Polimórfico:** Executa dinamicamente as estratégias ativas sem engessar as regras no banco ou na requisição.
4. **Reclassificação Concorrente:** A reclassificação da fila não bloqueia a resposta da requisição REST, sendo delegada para uma thread em background (`Runnable`).

