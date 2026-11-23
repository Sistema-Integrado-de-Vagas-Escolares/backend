package com.sive.service;

import com.sive.dao.Conexao;
import com.sive.dao.InscricaoDAO;
import com.sive.dao.TurmaDao;
import com.sive.exception.AcessoNegadoException;
import com.sive.exception.InscricaoNaoEncontradaException;
import com.sive.exception.StatusInvalidoException;
import com.sive.exception.VagaIndisponivelException;
import com.sive.model.GerenciarChamadoRequest;
import com.sive.model.InscricaoEscolaResumo;
import com.sive.model.ListaInscricoesEscolaResponse;
import com.sive.model.StatusInscricao;
import com.sive.model.Turma;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InscricaoService {

    private final InscricaoDAO inscricaoDAO;
    private final TurmaDao turmaDao;

    public InscricaoService() {
        this.inscricaoDAO = new InscricaoDAO();
        this.turmaDao = new TurmaDao();
    }

    public ListaInscricoesEscolaResponse listarPorEscola(
            UUID escolaId,
            StatusInscricao status,
            String busca,
            String serieAno) throws SQLException {
        StatusInscricao statusAlvo = status != null ? status : StatusInscricao.EM_ESPERA;

        List<InscricaoEscolaResumo> itens = inscricaoDAO.listarPorEscola(escolaId, statusAlvo, busca, serieAno);
        Map<StatusInscricao, Integer> contagens = inscricaoDAO.contarPorStatus(escolaId);

        return new ListaInscricoesEscolaResponse(
                itens,
                contagens.getOrDefault(StatusInscricao.EM_ESPERA, 0),
                contagens.getOrDefault(StatusInscricao.CHAMADO, 0),
                contagens.getOrDefault(StatusInscricao.MATRICULADO, 0));
    }

    public void chamar(UUID escolaId, UUID inscricaoId) throws SQLException {
        InscricaoEscolaResumo inscricao = inscricaoDAO.buscarPorIdEEscola(inscricaoId, escolaId);
        if (inscricao == null) {
            throw new InscricaoNaoEncontradaException("Inscrição não encontrada para a escola autenticada.");
        }
        if (inscricao.getStatus() != StatusInscricao.EM_ESPERA) {
            throw new StatusInvalidoException("Apenas inscrições em espera podem ser chamadas.");
        }
        inscricaoDAO.chamar(inscricaoId);
    }

    public void gerenciarChamado(
            UUID escolaId,
            UUID inscricaoId,
            GerenciarChamadoRequest request) throws SQLException {
        if (request == null || request.getAcao() == null || request.getAcao().trim().isEmpty()) {
            throw new IllegalArgumentException("A ação para gerenciar o chamado é obrigatória.");
        }

        InscricaoEscolaResumo inscricao = inscricaoDAO.buscarPorIdEEscola(inscricaoId, escolaId);
        if (inscricao == null) {
            throw new InscricaoNaoEncontradaException("Inscrição não encontrada para a escola autenticada.");
        }
        if (inscricao.getStatus() != StatusInscricao.CHAMADO) {
            throw new StatusInvalidoException("Apenas inscrições com chamado pendente podem ser gerenciadas.");
        }

        String acao = request.getAcao().trim().toUpperCase();
        switch (acao) {
            case "ACEITAR" -> aceitarChamado(escolaId, inscricao, request.getTurmaId());
            case "RECUSAR", "DESISTIR" -> atualizarSimples(inscricaoId, StatusInscricao.CANCELADO, false);
            case "REPASSAR" -> atualizarSimples(inscricaoId, StatusInscricao.EXPIRADO, false);
            case "DESFAZER" -> atualizarSimples(inscricaoId, StatusInscricao.EM_ESPERA, true);
            default -> throw new IllegalArgumentException("Ação de chamado inválida: " + request.getAcao());
        }
    }

    private void aceitarChamado(
            UUID escolaId,
            InscricaoEscolaResumo inscricao,
            UUID turmaIdSolicitada) throws SQLException {
        UUID turmaIdDestino = turmaIdSolicitada != null ? turmaIdSolicitada : inscricao.getTurmaId();
        if (turmaIdDestino == null) {
            throw new IllegalArgumentException("Selecione a turma para confirmar a matrícula.");
        }

        Turma turma = turmaDao.buscarPorId(turmaIdDestino);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada para o ID informado.");
        }
        if (!escolaId.equals(turma.getEscolaId())) {
            throw new AcessoNegadoException("A turma selecionada não pertence à escola autenticada.");
        }
        if (turma.getVagasDisponiveis() <= 0) {
            throw new VagaIndisponivelException("Não há vagas disponíveis na turma selecionada.");
        }

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean decrementou = inscricaoDAO.decrementarVagaTurma(conn, turmaIdDestino);
                if (!decrementou) {
                    throw new VagaIndisponivelException("Não há vagas disponíveis na turma selecionada.");
                }
                inscricaoDAO.atualizarStatusChamado(
                        conn,
                        inscricao.getId(),
                        StatusInscricao.MATRICULADO,
                        turmaIdDestino,
                        false);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void atualizarSimples(
            UUID inscricaoId,
            StatusInscricao novoStatus,
            boolean limparDataChamada) throws SQLException {
        try (Connection conn = Conexao.getConnection()) {
            inscricaoDAO.atualizarStatusChamado(conn, inscricaoId, novoStatus, null, limparDataChamada);
        }
    }
}
