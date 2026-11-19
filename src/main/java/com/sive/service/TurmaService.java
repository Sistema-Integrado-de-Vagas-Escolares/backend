package com.sive.service;

import java.sql.SQLException;
import java.util.UUID;

import com.sive.dao.TurmaDao;
import com.sive.exception.VagaIndisponivelException;
import com.sive.model.Turma;

public class TurmaService {
    private final TurmaDao turmaDao;

    public TurmaService(TurmaDao turmaDAO) {
        this.turmaDao = turmaDAO;
    }

    public synchronized void alocarVaga(UUID turmaId) throws SQLException {
        Turma turma = turmaDao.buscarPorId(turmaId);

        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada");

        }

        if (turma.getVagasDisponiveis() <= 0) {
            throw new VagaIndisponivelException("Não há vagas disponíveis nesta turma");
        }

        turmaDao.atualizarSaldoVagas(turmaId, turma.getVagasDisponiveis() - 1);
    }
}
