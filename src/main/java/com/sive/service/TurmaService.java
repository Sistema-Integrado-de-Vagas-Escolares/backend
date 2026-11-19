package com.sive.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sive.dao.EscolaDAO;
import com.sive.dao.TurmaDao;
import com.sive.exception.VagaIndisponivelException;
import com.sive.model.Escola;
import com.sive.model.Turma;
import com.sive.tarefa.TarefaRecalcularFila;

/**
 * Camada de serviço responsável pelas regras de negócio e validações da
 * entidade Turma.
 */
@Service
public class TurmaService {

    private final TurmaDao turmaDao;
    private final EscolaDAO escolaDAO;

    public TurmaService() {
        this.turmaDao = new TurmaDao();
        this.escolaDAO = new EscolaDAO();
    }

    public TurmaService(TurmaDao turmaDao, EscolaDAO escolaDAO) {
        this.turmaDao = turmaDao;
        this.escolaDAO = escolaDAO;
    }

    /**
     * Cadastra uma nova turma após aplicar validações de negócio e capacidade da
     * escola.
     */
    public Turma cadastrar(Turma turma) throws SQLException {
        if (turma == null) {
            throw new IllegalArgumentException("Os dados da turma não podem ser nulos.");
        }

        if (turma.getEscolaId() == null) {
            throw new IllegalArgumentException("O id da escola é obrigatório.");
        }

        if (turma.getSerieAno() == null || turma.getSerieAno().trim().isEmpty()) {
            throw new IllegalArgumentException("A série/ano da turma é obrigatório.");
        }

        if (turma.getTurno() == null) {
            throw new IllegalArgumentException("O turno da turma é obrigatório.");
        }

        if (turma.getVagasTotais() <= 0) {
            throw new IllegalArgumentException("A quantidade de vagas totais deve ser maior que zero.");
        }

        // 1. Verifica se a escola informada existe
        Escola escola = escolaDAO.buscarPorId(turma.getEscolaId());
        if (escola == null) {
            throw new IllegalArgumentException("Escola não encontrada para o id informado: " + turma.getEscolaId());
        }

        // 2. Valida se a capacidade total da escola suporta a nova turma
        List<Turma> turmasExistentes = turmaDao.listarPorEscola(turma.getEscolaId());
        int totalVagasExistentes = turmasExistentes.stream().mapToInt(Turma::getVagasTotais).sum();

        if (totalVagasExistentes + turma.getVagasTotais() > escola.getCapacidadeTotal()) {
            throw new IllegalArgumentException("A capacidade total da escola (" + escola.getCapacidadeTotal()
                    + ") seria excedida. Vagas ja alocadas: " + totalVagasExistentes
                    + ", vagas da nova turma: " + turma.getVagasTotais());
        }

        // 3. Inicializa as vagas disponíveis com o total de vagas da turma
        turma.setVagasDisponiveis(turma.getVagasTotais());

        return turmaDao.inserir(turma);
    }

    /**
     * Lista todas as turmas de uma escola específica.
     */
    public List<Turma> listarPorEscola(UUID escolaId) throws SQLException {
        if (escolaId == null) {
            throw new IllegalArgumentException("O id da escola não pode ser nulo.");
        }
        return turmaDao.listarPorEscola(escolaId);
    }

    /**
     * Busca uma turma pelo seu identificador único.
     */
    public Turma buscarPorId(UUID id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("O id da turma não pode ser nulo.");
        }
        return turmaDao.buscarPorId(id);
    }

    /**
     * Dispara o recalculado concorrente da fila de espera de uma turma em uma
     * Thread separada.
     */
    public void recalcularFilaAssincrono(UUID turmaId) throws SQLException {
        Turma turma = buscarPorId(turmaId);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada para o id informado: " + turmaId);
        }

        TarefaRecalcularFila tarefa = new TarefaRecalcularFila(turmaId);
        Thread thread = new Thread(tarefa, "Thread-RecalcularFila-" + turmaId.toString().substring(0, 8));
        thread.start();
    }

    /**
     * Realiza a alocação de vaga com controle de concorrência.
     */
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
