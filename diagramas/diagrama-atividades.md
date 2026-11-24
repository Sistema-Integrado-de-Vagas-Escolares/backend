# Diagrama de Atividades — SIVE

O Diagrama de Atividades ilustra o fluxo completo do processo de **Cadastro Familiar, Cálculo de Pontuação e Inscrição na Fila de Espera**, incorporando a criptografia de dados sensíveis (LGPD) e o motor Strategy.

---

## Diagrama (Mermaid)

```mermaid
flowchart TD
    Inicio([Início: Cidadão preenche solicitação de vaga]) --> ReceberPayload["Receber payload da inscrição no Controller"]
    ReceberPayload --> ValidarDados{"Dados obrigatórios válidos?"}

    ValidarDados -- Não --> ErroValidacao["Lançar IllegalArgumentException (HTTP 400)"] --> FimErro([Fim com Erro])
    ValidarDados -- Sim --> ProcessarCripto["CpfCryptoService: Criptografar CPF (AES) e gerar Hash SHA-256"]

    ProcessarCripto --> BuscarAluno{"Aluno já existe na base?"}
    BuscarAluno -- Sim --> ObterAluno["Recuperar dados do Aluno existente"]
    BuscarAluno -- Não --> CriarAluno["Persistir novo Aluno com Endereço e Dados Socioeconômicos"]

    ObterAluno --> AssociarResponsavel["Vincular Responsável Legal (AlunoResponsavel N:N)"]
    CriarAluno --> AssociarResponsavel

    AssociarResponsavel --> VerificarDuplicidade{"Aluno já possui inscrição ativa nesta turma?"}
    VerificarDuplicidade -- Sim --> LancarDuplicado["Lançar AlunoJaInscritoException (HTTP 409)"] --> FimErro

    VerificarDuplicidade -- Não --> GerarProtocolo["Gerar código único de protocolo da inscrição"]

    GerarProtocolo --> BuscarCriterios["Consultar critérios de pontuação ativos no banco (CriterioDAO)"]
    BuscarCriterios --> IniciarStrategy["Iterar critérios via CriterioStrategyFactory"]

    subgraph CalculoStrategy["Motor de Pontuação Strategy"]
        direction TB
        CriterioRenda["CriterioRendaStrategy: Avaliar renda per capita"]
        CriterioBool["CriterioBooleanoStrategy: Avaliar flags\n(Deficiência, Mãe solo, Aluguel, Bolsa Família, BPC, Irmão)"]
        CriterioManual["CriterioSemRegraAutomaticaStrategy: Critérios manuais"]
        SomarPontos["Somar pontuações obtidas e registrar em InscricaoCriterio"]
        CriterioRenda --> SomarPontos
        CriterioBool --> SomarPontos
        CriterioManual --> SomarPontos
    end

    IniciarStrategy --> CalculoStrategy
    CalculoStrategy --> DefinirStatus["Definir Status = EM_ESPERA e pontuação total"]

    DefinirStatus --> PersistirInscricao["Persistir Inscrição no Banco (InscricaoDAO)"]
    PersistirInscricao --> DispararThread["Disparar TarefaRecalcularFila em Thread paralela"]

    subgraph BackgroundAsync["Processamento Concorrente em Segundo Plano"]
        RecuperarFila["Buscar todas as inscrições EM_ESPERA da turma"]
        RecuperarFila --> Ordenar["Ordenar por pontuação DESC e data de inscrição ASC"]
        Ordenar --> AtualizarPosicoes["Atualizar coluna posicao_fila em lote (executeBatch)"]
    end

    DispararThread -.-> BackgroundAsync
    DispararThread --> RetornarSucesso["Retornar HTTP 201 com Protocolo, Posição e Detalhamento de Pontos"]
    RetornarSucesso --> FimSucesso([Fim com Sucesso])
```

---

## Principais Etapas e Decisões do Fluxo

1. **Tratamento de Privacidade (LGPD):** O CPF do aluno e dos responsáveis é imediatamente cifrado em formato reversível seguro (`AES-GCM`) para armazenamento e tem um hash (`SHA-256`) gerado para viabilizar pesquisas sem decifrar registros em massa.
2. **Prevenção de Inscrições Duplicadas:** O sistema bloqueia requisições duplicadas para a mesma turma lançando `AlunoJaInscritoException` (HTTP 409).
3. **Cálculo Desacoplado via Strategy:** Cada critério ativo é avaliado por uma estratégia específica, garantindo que novas regras socioeconômicas possam ser introduzidas sem alterar o fluxo principal de inscrição.
4. **Reclassificação Assíncrona:** A fila da turma é reordenada concorrentemente via `TarefaRecalcularFila`, permitindo que a resposta HTTP seja entregue de imediato ao cidadão.
