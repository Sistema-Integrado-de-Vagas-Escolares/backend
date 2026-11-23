package com.sive.service;

import com.sive.dao.HistoricoVagaDAO;
import com.sive.model.AtualizarVagasRequest;
import com.sive.model.HistoricoVaga;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Regras de negócio para gestão e histórico de disponibilidade de vagas.
 */
@Service
public class HistoricoVagaService {

    private final HistoricoVagaDAO historicoVagaDAO;

    public HistoricoVagaService(HistoricoVagaDAO historicoVagaDAO) {
        this.historicoVagaDAO = historicoVagaDAO;
    }

    public List<HistoricoVaga> listarPorEscola(UUID escolaId, UUID turmaId) throws SQLException {
        return historicoVagaDAO.listarPorEscola(escolaId, turmaId);
    }

    public HistoricoVaga atualizarVagas(UUID escolaId, AtualizarVagasRequest request) throws SQLException {
        if (request == null || request.getTurmaId() == null) {
            throw new IllegalArgumentException("Selecione a turma para atualizar as vagas.");
        }
        if (request.getVagasDisponiveis() == null || request.getVagasDisponiveis() < 0) {
            throw new IllegalArgumentException("Informe uma quantidade válida de vagas disponíveis.");
        }
        if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo da atualização é obrigatório.");
        }

        return historicoVagaDAO.atualizarVagasERegistrarHistorico(
                escolaId,
                request.getTurmaId(),
                request.getVagasDisponiveis(),
                request.getMotivo().trim());
    }
}
