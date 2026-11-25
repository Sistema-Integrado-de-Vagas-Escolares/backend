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
        turma.setSerieAno(turma.getSerieAno().trim());

        String nomeNormalizado = turma.getNome() != null && !turma.getNome().trim().isEmpty()
                ? turma.getNome().trim()
                : "Turma A";
        turma.setNome(nomeNormalizado);

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

        // 2. Valida duplicidade e se a capacidade total da escola suporta a nova turma
        List<Turma> turmasExistentes = turmaDao.listarPorEscola(turma.getEscolaId());
        for (Turma existente : turmasExistentes) {
            if (existente.getSerieAno().equalsIgnoreCase(turma.getSerieAno())
                    && existente.getTurno() == turma.getTurno()
                    && existente.getNome() != null
                    && existente.getNome().equalsIgnoreCase(nomeNormalizado)) {
                throw new IllegalArgumentException(
                        "Já existe uma turma \"" + nomeNormalizado + "\" para "
                                + turma.getSerieAno() + " no turno selecionado.");
            }
        }
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

    public Turma buscarPorIdDaEscola(UUID escolaId, UUID turmaId) throws SQLException {
        Turma turma = buscarPorId(turmaId);
        if (turma == null || !escolaId.equals(turma.getEscolaId())) {
            throw new IllegalArgumentException("Turma não encontrada para esta escola.");
        }
        return turma;
    }

    public Turma atualizar(UUID escolaId, UUID turmaId, Turma dados) throws SQLException {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados da turma não podem ser nulos.");
        }

        Turma atual = buscarPorIdDaEscola(escolaId, turmaId);

        if (dados.getSerieAno() == null || dados.getSerieAno().trim().isEmpty()) {
            throw new IllegalArgumentException("A série/ano da turma é obrigatório.");
        }
        String serieAno = dados.getSerieAno().trim();
        String nome = dados.getNome() != null && !dados.getNome().trim().isEmpty()
                ? dados.getNome().trim()
                : "Turma A";

        if (dados.getTurno() == null) {
            throw new IllegalArgumentException("O turno da turma é obrigatório.");
        }
        if (dados.getVagasTotais() <= 0) {
            throw new IllegalArgumentException("A quantidade de vagas totais deve ser maior que zero.");
        }

        int ocupadas = Math.max(0, atual.getVagasTotais() - atual.getVagasDisponiveis());
        if (dados.getVagasTotais() < ocupadas) {
            throw new IllegalArgumentException(
                    "O total de vagas não pode ser menor que as " + ocupadas + " vagas já ocupadas.");
        }

        Escola escola = escolaDAO.buscarPorId(escolaId);
        if (escola == null) {
            throw new IllegalArgumentException("Escola não encontrada.");
        }

        List<Turma> turmasExistentes = turmaDao.listarPorEscola(escolaId);
        int vagasOutrasTurmas = 0;
        for (Turma existente : turmasExistentes) {
            if (existente.getId().equals(turmaId)) {
                continue;
            }
            vagasOutrasTurmas += existente.getVagasTotais();
            if (existente.getSerieAno().equalsIgnoreCase(serieAno)
                    && existente.getTurno() == dados.getTurno()
                    && existente.getNome() != null
                    && existente.getNome().equalsIgnoreCase(nome)) {
                throw new IllegalArgumentException(
                        "Já existe uma turma \"" + nome + "\" para "
                                + serieAno + " no turno selecionado.");
            }
        }

        if (vagasOutrasTurmas + dados.getVagasTotais() > escola.getCapacidadeTotal()) {
            throw new IllegalArgumentException(
                    "A capacidade total da escola (" + escola.getCapacidadeTotal() + ") seria excedida.");
        }

        atual.setSerieAno(serieAno);
        atual.setNome(nome);
        atual.setTurno(dados.getTurno());
        atual.setVagasTotais(dados.getVagasTotais());
        atual.setVagasDisponiveis(dados.getVagasTotais() - ocupadas);

        return turmaDao.atualizar(atual);
    }

    public void excluir(UUID escolaId, UUID turmaId) throws SQLException {
        buscarPorIdDaEscola(escolaId, turmaId);
        int inscricoesVinculadas = turmaDao.contarInscricoesVinculadas(turmaId);
        if (inscricoesVinculadas > 0) {
            throw new IllegalArgumentException(
                    "Não é possível excluir a turma pois existem "
                            + inscricoesVinculadas + " aluno(s)/inscrição(ões) vinculados.");
        }
        turmaDao.excluir(escolaId, turmaId);
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
