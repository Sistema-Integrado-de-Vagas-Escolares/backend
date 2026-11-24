# Diagrama de Sequência — SIVE

O Diagrama de Sequência ilustra a operação crítica do painel da escola para **Gerenciamento de Inscrição: Convocação, Concorrência e Efetivação de Matrícula** (`POST /api/escola-painel/inscricoes/{id}/gerenciar`), exibindo a validação de token, segurança, auditoria e controle contra condições de corrida (*race conditions*).

---

## Diagrama (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Gestor as Gestor Escolar
    participant Interceptor as EscolaAuthInterceptor
    participant TokenSvc as TokenService
    participant Controller as EscolaPainelController
    participant InscricaoSvc as InscricaoService
    participant TurmaSvc as TurmaService
    participant InscricaoDAO as InscricaoDAO
    participant TurmaDao as TurmaDao
    participant HistVagaDAO as HistoricoVagaDAO
    participant DB as PostgreSQL 16 (sive_db)

    Gestor ->> Interceptor: POST /api/escola-painel/inscricoes/{id}/gerenciar\n[Headers: Authorization Bearer <token>]
    activate Interceptor

    Interceptor ->> TokenSvc: validarToken(token)
    TokenSvc -->> Interceptor: token válido (escolaId autenticada)

    alt Token inválido ou expirado
        Interceptor -->> Gestor: HTTP 401 Unauthorized (AutenticacaoException)
    end

    Interceptor ->> Controller: encaminha requisição (payload: {acao: "MATRICULAR"})
    deactivate Interceptor
    activate Controller

    Controller ->> InscricaoSvc: gerenciarChamado(inscricaoId, escolaId, "MATRICULAR")
    activate InscricaoSvc

    InscricaoSvc ->> InscricaoDAO: buscarPorId(inscricaoId)
    InscricaoDAO ->> DB: SELECT * FROM inscricoes WHERE id = ?
    DB -->> InscricaoDAO: dados da inscricao
    InscricaoDAO -->> InscricaoSvc: Inscricao (status, turma_id, escola_id)

    alt Inscrição não pertence à escola autenticada
        InscricaoSvc -->> Controller: throws AcessoNegadoException
        Controller -->> Gestor: HTTP 403 Forbidden
    else Status atual diferente de CHAMADO
        InscricaoSvc -->> Controller: throws StatusInvalidoException
        Controller -->> Gestor: HTTP 400 Bad Request
    end

    Note over InscricaoSvc, TurmaSvc: Bloco Sincronizado (Thread-safe: synchronized alocarVaga)
    InscricaoSvc ->> TurmaSvc: alocarVaga(turmaId)
    activate TurmaSvc

    TurmaSvc ->> TurmaDao: buscarPorId(turmaId)
    TurmaDao ->> DB: SELECT vagas_disponiveis FROM turmas WHERE id = ?
    DB -->> TurmaDao: Turma (vagas_disponiveis)
    TurmaDao -->> TurmaSvc: Turma

    alt vagas_disponiveis <= 0
        TurmaSvc -->> InscricaoSvc: throws VagaIndisponivelException
        InscricaoSvc -->> Controller: throws VagaIndisponivelException
        Controller -->> Gestor: HTTP 409 Conflict (Não há vagas disponíveis)
    end

    TurmaSvc ->> TurmaDao: atualizarSaldoVagas(turmaId, saldoAnterior - 1)
    TurmaDao ->> DB: UPDATE turmas SET vagas_disponiveis = ? WHERE id = ?
    DB -->> TurmaDao: 1 linha atualizada
    TurmaDao -->> TurmaSvc: ok

    TurmaSvc ->> HistVagaDAO: inserir(HistoricoVaga: motivo = "Matrícula efetivada")
    HistVagaDAO ->> DB: INSERT INTO historico_vagas (...)
    DB -->> HistVagaDAO: registro salvo
    TurmaSvc -->> InscricaoSvc: vaga decrementada e auditada
    deactivate TurmaSvc

    InscricaoSvc ->> InscricaoDAO: atualizarStatus(inscricaoId, MATRICULADO)
    InscricaoDAO ->> DB: UPDATE inscricoes SET status = 'MATRICULADO' WHERE id = ?
    DB -->> InscricaoDAO: 1 linha atualizada
    InscricaoDAO -->> InscricaoSvc: ok

    InscricaoSvc -->> Controller: Inscrição atualizada para MATRICULADO
    deactivate InscricaoSvc

    Controller -->> Gestor: HTTP 200 OK (Mensagem de sucesso e status atualizado)
    deactivate Controller
```

---

## Aspectos Arquiteturais Evidenciados

1. **Autenticação e Autorização por Interceptor:** Toda requisição ao painel é interceptada e validada pelo `EscolaAuthInterceptor` através do token de sessão antes de alcançar o controlador.
2. **Isolamento Multitenant por Escola:** A `InscricaoService` valida se a inscrição gerenciada pertence estritamente à escola vinculada ao token autenticado (`AcessoNegadoException` com HTTP 403 em caso de violação).
3. **Prevenção de Condições de Corrida (*Race Conditions*):** A execução do método `TurmaService.alocarVaga` é protegida com `synchronized`, assegurando que requisições concorrentes não ultrapassem o limite de capacidade da turma.
4. **Auditoria Contínua:** Toda mudança na quantidade de vagas disponíveis gera um registro imutável em `historico_vagas`.
