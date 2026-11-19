# Diagrama de Sequência — SIVE

O Diagrama de Sequência detalha a troca de mensagens para a operação crítica de **Efetivação de Matrícula com Controle de Concorrência** (`POST /api/inscricoes/{id}/matricular`), demonstrando a proteção contra condições de corrida (*race conditions*) no decremento de vagas.

---

## Diagrama (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Gestor as Gestor Escolar
    participant Controller as InscricaoController
    participant InscricaoSvc as InscricaoService
    participant TurmaSvc as TurmaService
    participant InscricaoDAO as InscricaoDAO
    participant TurmaDAO as TurmaDAO
    participant DB as PostgreSQL (sive_db)

    Gestor ->> Controller: POST /api/inscricoes/{id}/matricular
    activate Controller

    Controller ->> InscricaoSvc: matricular(inscricaoId)
    activate InscricaoSvc

    InscricaoSvc ->> InscricaoDAO: buscarPorId(inscricaoId)
    InscricaoDAO ->> DB: SELECT * FROM inscricoes WHERE id = ?
    DB -->> InscricaoDAO: dados da inscricao
    InscricaoDAO -->> InscricaoSvc: Inscricao

    alt Inscrição não encontrada
        InscricaoSvc -->> Controller: throws InscricaoNaoEncontradaException
        Controller -->> Gestor: HTTP 404 (Not Found)
    else Status diferente de CHAMADO
        InscricaoSvc -->> Controller: throws StatusInvalidoException
        Controller -->> Gestor: HTTP 400 (Bad Request)
    end

    Note over InscricaoSvc, TurmaSvc: Bloco Crítico de Concorrência (synchronized)
    InscricaoSvc ->> TurmaSvc: alocarVaga(turmaId)
    activate TurmaSvc

    TurmaSvc ->> TurmaDAO: buscarPorId(turmaId)
    TurmaDAO ->> DB: SELECT vagas_disponiveis FROM turmas WHERE id = ?
    DB -->> TurmaDAO: Turma (vagas_disponiveis)
    TurmaDAO -->> TurmaSvc: Turma

    alt vagasDisponiveis <= 0
        TurmaSvc -->> InscricaoSvc: throws VagaIndisponivelException
        InscricaoSvc -->> Controller: throws VagaIndisponivelException
        Controller -->> Gestor: HTTP 409 (Conflict - Vagas Esgotadas)
    end

    TurmaSvc ->> TurmaDAO: decrementarVaga(turmaId)
    TurmaDAO ->> DB: UPDATE turmas SET vagas_disponiveis = vagas_disponiveis - 1 WHERE id = ?
    DB -->> TurmaDAO: 1 row updated
    TurmaDAO -->> TurmaSvc: ok
    TurmaSvc -->> InscricaoSvc: vaga confirmada
    deactivate TurmaSvc

    InscricaoSvc ->> InscricaoDAO: atualizarStatus(inscricaoId, MATRICULADO)
    InscricaoDAO ->> DB: UPDATE inscricoes SET status = 'MATRICULADO' WHERE id = ?
    DB -->> InscricaoDAO: 1 row updated
    InscricaoDAO -->> InscricaoSvc: ok

    InscricaoSvc -->> Controller: Inscricao atualizada
    deactivate InscricaoSvc

    Controller -->> Gestor: HTTP 200 OK (Dados da Inscrição com Status MATRICULADO)
    deactivate Controller
```

---

## Aspectos Críticos Ilustrados no Fluxo

1. **Validação de Pré-condição:** Apenas inscrições que já estão no status `CHAMADO` podem ser migradas para `MATRICULADO`. Caso contrário, uma `StatusInvalidoException` é capturada pelo `GlobalExceptionHandler` gerando HTTP 400.
2. **Prevenção de Race Condition:** A chamada para `TurmaService.alocarVaga(...)` é protegida com `synchronized` para que duas requisições simultâneas não tentem alocar a última vaga disponível ao mesmo tempo.
3. **Atomicidade:** A validação de disponibilidade e o decremento no banco ocorrem de forma consistente antes da transição do status da inscrição.

