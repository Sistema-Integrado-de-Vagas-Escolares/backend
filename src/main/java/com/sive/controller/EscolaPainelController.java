package com.sive.controller;

import com.sive.exception.AcessoNegadoException;
import com.sive.model.Escola;
import com.sive.model.Turma;
import com.sive.security.EscolaAuthInterceptor;
import com.sive.service.EscolaService;
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

/**
 * Controlador REST com as rotas autenticadas acessadas pela escola logada (/api/escola/**).
 */
@RestController
@RequestMapping("/api/escola")
public class EscolaPainelController {

    private final EscolaService escolaService;
    private final TurmaService turmaService;

    public EscolaPainelController(EscolaService escolaService, TurmaService turmaService) {
        this.escolaService = escolaService;
        this.turmaService = turmaService;
    }

    /**
     * Retorna os dados cadastrais da escola autenticada.
     * GET /api/escola/perfil
     */
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

    /**
     * Lista todas as turmas da escola autenticada.
     * GET /api/escola/turmas
     */
    @GetMapping("/turmas")
    public ResponseEntity<List<Turma>> listarTurmas(HttpServletRequest request) throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        return ResponseEntity.ok(turmaService.listarPorEscola(escolaId));
    }

    /**
     * Cadastra uma nova turma vinculada automaticamente à escola autenticada.
     * POST /api/escola/turmas
     */
    @PostMapping("/turmas")
    public ResponseEntity<Turma> cadastrarTurma(@RequestBody Turma turma, HttpServletRequest request)
            throws SQLException {
        UUID escolaId = obterEscolaIdAutenticada(request);
        turma.setEscolaId(escolaId);
        Turma criada = turmaService.cadastrar(turma);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    /**
     * Dispara o recálculo concorrente da fila de espera de uma turma pertencente à escola autenticada.
     * POST /api/escola/turmas/{turmaId}/recalcular
     */
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

    private UUID obterEscolaIdAutenticada(HttpServletRequest request) {
        Object atributo = request.getAttribute(EscolaAuthInterceptor.ATRIBUTO_ESCOLA_ID);
        if (atributo instanceof UUID) {
            return (UUID) atributo;
        }
        throw new IllegalStateException("Identificador da escola autenticada não encontrado na requisição.");
    }
}
