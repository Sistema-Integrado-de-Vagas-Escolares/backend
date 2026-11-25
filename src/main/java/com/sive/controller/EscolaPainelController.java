package com.sive.controller;

import com.sive.exception.AcessoNegadoException;
import com.sive.model.AlunoDetalhe;
import com.sive.model.AlunoResumo;
import com.sive.model.AtualizarVagasRequest;
import com.sive.model.CadastrarAlunoInscricaoRequest;
import com.sive.model.Criterio;
import com.sive.model.Escola;
import com.sive.model.GerenciarChamadoRequest;
import com.sive.model.HistoricoVaga;
import com.sive.model.ListaInscricoesEscolaResponse;
import com.sive.model.Responsavel;
import com.sive.model.ResponsavelDetalhe;
import com.sive.model.ResponsavelResumo;
import com.sive.model.ResumoEscola;
import com.sive.model.StatusInscricao;
import com.sive.model.Turma;
import com.sive.model.AtualizarProximidadeRequest;
import com.sive.security.EscolaAuthInterceptor;
import com.sive.service.AlunoService;
import com.sive.service.CriterioService;
import com.sive.service.EscolaService;
import com.sive.service.HistoricoVagaService;
import com.sive.service.InscricaoService;
import com.sive.service.ResponsavelService;
import com.sive.service.TurmaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/escola")
public class EscolaPainelController {

    private final EscolaService escolaService;
    private final TurmaService turmaService;
    private final ResponsavelService responsavelService;
    private final AlunoService alunoService;
    private final InscricaoService inscricaoService;
    private final CriterioService criterioService;
    private final HistoricoVagaService historicoVagaService;

    public EscolaPainelController(
            EscolaService escolaService,
            TurmaService turmaService,
            ResponsavelService responsavelService,
            AlunoService alunoService,
            InscricaoService inscricaoService,
            CriterioService criterioService,
            HistoricoVagaService historicoVagaService) {
        this.escolaService = escolaService;
        this.turmaService = turmaService;
        this.responsavelService = responsavelService;
        this.alunoService = alunoService;
        this.inscricaoService = inscricaoService;
        this.criterioService = criterioService;
        this.historicoVagaService = historicoVagaService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        Escola escola = escolaService.buscarPorId(escolaId);
        if (escola == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Escola autenticada não encontrada.");
        }
        return ResponseEntity.ok(escola);
    }

    @GetMapping("/resumo")
    public ResponseEntity<?> resumo(HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        ResumoEscola resumo = escolaService.obterResumo(escolaId);
        if (resumo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Escola autenticada não encontrada.");
        }
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/turmas")
    public ResponseEntity<List<Turma>> listarTurmas(HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(turmaService.listarPorEscola(escolaId));
    }

    @PostMapping("/turmas")
    public ResponseEntity<Turma> cadastrarTurma(@RequestBody Turma turma, HttpServletRequest request)
            throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        turma.setEscolaId(escolaId);
        Turma criada = turmaService.cadastrar(turma);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping("/turmas/{turmaId}")
    public ResponseEntity<Turma> buscarTurma(
            @PathVariable UUID turmaId,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(turmaService.buscarPorIdDaEscola(escolaId, turmaId));
    }

    @PutMapping("/turmas/{turmaId}")
    public ResponseEntity<Turma> atualizarTurma(
            @PathVariable UUID turmaId,
            @RequestBody Turma turma,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(turmaService.atualizar(escolaId, turmaId, turma));
    }

    @DeleteMapping("/turmas/{turmaId}")
    public ResponseEntity<Map<String, Object>> excluirTurma(
            @PathVariable UUID turmaId,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        turmaService.excluir(escolaId, turmaId);
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Turma excluída com sucesso.");
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/turmas/{turmaId}/recalcular")
    public ResponseEntity<Map<String, Object>> recalcularFila(
            @PathVariable UUID turmaId,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        Turma turma = turmaService.buscarPorId(turmaId);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada para o id informado: " + turmaId);
        }
        if (!escolaId.equals(turma.getEscolaId())) {
            throw new AcessoNegadoException("Você não tem permissão para recalcular a fila de outra escola.");
        }

        turmaService.recalcularFilaAssincrono(turmaId);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Recálculo da fila iniciado em segundo plano com sucesso.");
        resposta.put("turmaId", turmaId);
        return ResponseEntity.accepted().body(resposta);
    }

    @GetMapping("/responsaveis")
    public ResponseEntity<List<ResponsavelResumo>> listarResponsaveis(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean comFilhos,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(responsavelService.listarPorEscola(escolaId, busca, comFilhos));
    }

    @GetMapping("/responsaveis/{responsavelId}")
    public ResponseEntity<ResponsavelDetalhe> buscarResponsavelDetalhe(
            @PathVariable UUID responsavelId,
            @RequestParam(defaultValue = "false") boolean incluirCpfCompleto,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(
                responsavelService.buscarDetalhePorEscola(escolaId, responsavelId, incluirCpfCompleto));
    }

    @PostMapping("/responsaveis")
    public ResponseEntity<Responsavel> cadastrarResponsavel(
            @RequestBody Responsavel responsavel,
            HttpServletRequest request) throws SQLException {
        obterEscolaIdAutenticada(request);
        Responsavel criado = responsavelService.cadastrar(responsavel);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/responsaveis/{responsavelId}")
    public ResponseEntity<ResponsavelDetalhe> atualizarResponsavel(
            @PathVariable UUID responsavelId,
            @RequestBody Responsavel responsavel,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(responsavelService.atualizarParaEscola(escolaId, responsavelId, responsavel));
    }

    @GetMapping("/alunos")
    public ResponseEntity<List<AlunoResumo>> listarAlunos(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String serieAno,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(alunoService.listarPorEscola(escolaId, busca, serieAno));
    }

    @GetMapping("/alunos/{alunoId}")
    public ResponseEntity<AlunoDetalhe> buscarAlunoDetalhe(
            @PathVariable UUID alunoId,
            @RequestParam(defaultValue = "false") boolean incluirCpfCompleto,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(alunoService.buscarDetalhePorEscola(escolaId, alunoId, incluirCpfCompleto));
    }

    @PutMapping("/alunos/{alunoId}")
    public ResponseEntity<AlunoDetalhe> atualizarAluno(
            @PathVariable UUID alunoId,
            @RequestBody CadastrarAlunoInscricaoRequest payload,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(alunoService.atualizarParaEscola(escolaId, alunoId, payload));
    }

    @PatchMapping("/alunos/{alunoId}/proximidade")
    public ResponseEntity<AlunoDetalhe> atualizarProximidade(
            @PathVariable UUID alunoId,
            @RequestBody AtualizarProximidadeRequest payload,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        if (payload == null) {
            throw new IllegalArgumentException("A decisão de proximidade é obrigatória.");
        }
        return ResponseEntity.ok(alunoService.atualizarProximidadeParaEscola(
                escolaId, alunoId, payload.isAtendeProximidade()));
    }

    @PostMapping("/alunos")
    public ResponseEntity<AlunoResumo> cadastrarAluno(
            @RequestBody CadastrarAlunoInscricaoRequest payload,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        AlunoResumo criado = alunoService.cadastrarComInscricao(escolaId, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/inscricoes")
    public ResponseEntity<ListaInscricoesEscolaResponse> listarInscricoes(
            @RequestParam(required = false) StatusInscricao status,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String serieAno,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(inscricaoService.listarPorEscola(escolaId, status, busca, serieAno));
    }

    @PostMapping("/inscricoes/{inscricaoId}/chamar")
    public ResponseEntity<Map<String, Object>> chamarInscricao(
            @PathVariable UUID inscricaoId,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        inscricaoService.chamar(escolaId, inscricaoId);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Chamado registrado com sucesso.");
        resposta.put("inscricaoId", inscricaoId);
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/inscricoes/{inscricaoId}/chamado")
    public ResponseEntity<Map<String, Object>> gerenciarChamado(
            @PathVariable UUID inscricaoId,
            @RequestBody GerenciarChamadoRequest payload,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        inscricaoService.gerenciarChamado(escolaId, inscricaoId, payload);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Chamado atualizado com sucesso.");
        resposta.put("inscricaoId", inscricaoId);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/criterios")
    public ResponseEntity<List<Criterio>> listarCriterios(HttpServletRequest request) throws SQLException {
        obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(criterioService.listarCriterios());
    }

    @PutMapping("/criterios")
    public ResponseEntity<List<Criterio>> publicarCriterios(
            @RequestBody List<Criterio> criterios,
            HttpServletRequest request) throws SQLException {
        obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(criterioService.publicarCriterios(criterios));
    }

    @GetMapping("/vagas/historico")
    public ResponseEntity<List<HistoricoVaga>> listarHistoricoVagas(
            @RequestParam(required = false) UUID turmaId,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(historicoVagaService.listarPorEscola(escolaId, turmaId));
    }

    @PatchMapping("/vagas")
    public ResponseEntity<HistoricoVaga> atualizarVagas(
            @RequestBody AtualizarVagasRequest payload,
            HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(historicoVagaService.atualizarVagas(escolaId, payload));
    }

    private UUID obterEscolaIdAutenticada(HttpServletRequest request) {
        Object atributo = request.getAttribute(EscolaAuthInterceptor.ATRIBUTO_ESCOLA_ID);
        if (atributo instanceof UUID) {
            return (UUID) atributo;
        }
        throw new IllegalStateException("Identificador da escola autenticada não encontrado na requisição.");
    }
}
