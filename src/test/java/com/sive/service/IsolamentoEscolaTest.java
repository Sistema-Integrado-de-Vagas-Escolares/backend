package com.sive.service;

import com.sive.dao.AlunoDAO;
import com.sive.dao.AlunoResponsavelDAO;
import com.sive.dao.Conexao;
import com.sive.dao.CriterioDAO;
import com.sive.dao.InscricaoCriterioDAO;
import com.sive.dao.InscricaoDAO;
import com.sive.dao.ResponsavelDAO;
import com.sive.dao.TurmaDao;
import com.sive.exception.AcessoNegadoException;
import com.sive.exception.InscricaoNaoEncontradaException;
import com.sive.exception.VagaIndisponivelException;
import com.sive.model.Aluno;
import com.sive.model.AlunoDetalhe;
import com.sive.model.CadastrarAlunoInscricaoRequest;
import com.sive.model.Endereco;
import com.sive.model.GerenciarChamadoRequest;
import com.sive.model.InscricaoEscolaResumo;
import com.sive.model.ListaInscricoesEscolaResponse;
import com.sive.model.Responsavel;
import com.sive.model.ResponsavelDetalhe;
import com.sive.model.ResponsavelResumo;
import com.sive.model.StatusInscricao;
import com.sive.model.Turma;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IsolamentoEscolaTest {

    @AfterEach
    void tearDown() {
        Conexao.setConexaoCustomizada(null);
    }

    @Test
    void umaEscolaConsegueAdministrarSuasPropriasInscricoes() throws SQLException {
        UUID escolaA = UUID.randomUUID();
        UUID inscricaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        InscricaoEscolaResumo inscricaoEmEspera = new InscricaoEscolaResumo(
                inscricaoId, UUID.randomUUID(), "Aluno A", LocalDate.of(2020, 1, 1),
                turmaId, "Creche", "CRECHE", "INTEGRAL", "SIVE-001", BigDecimal.ZERO,
                StatusInscricao.EM_ESPERA, LocalDateTime.now(), null, 1);

        InscricaoEscolaResumo inscricaoChamada = new InscricaoEscolaResumo(
                inscricaoId, UUID.randomUUID(), "Aluno A", LocalDate.of(2020, 1, 1),
                turmaId, "Creche", "CRECHE", "INTEGRAL", "SIVE-001", BigDecimal.ZERO,
                StatusInscricao.CHAMADO, LocalDateTime.now(), LocalDateTime.now(), 1);

        boolean[] chamou = {false};
        boolean[] atualizouStatus = {false};

        InscricaoDAO daoStub = new InscricaoDAO() {
            @Override
            public InscricaoEscolaResumo buscarPorIdEEscola(UUID id, UUID escola) {
                if (escolaA.equals(escola)) {
                    return chamou[0] ? inscricaoChamada : inscricaoEmEspera;
                }
                return null;
            }

            @Override
            public boolean chamar(UUID id, UUID escola) {
                chamou[0] = true;
                return true;
            }

            @Override
            public boolean atualizarStatusChamado(Connection conn, UUID id, UUID escola, StatusInscricao status, UUID turma, boolean limpar) {
                atualizouStatus[0] = true;
                return true;
            }

            @Override
            public boolean decrementarVagaTurma(Connection conn, UUID turma, UUID escola) {
                return true;
            }
        };

        TurmaDao turmaDaoStub = new TurmaDao() {
            @Override
            public Turma buscarPorId(UUID id) {
                Turma t = new Turma();
                t.setId(turmaId);
                t.setEscolaId(escolaA);
                t.setVagasDisponiveis(5);
                return t;
            }
        };

        InscricaoService service = new InscricaoService(daoStub, turmaDaoStub);

        assertDoesNotThrow(() -> service.chamar(escolaA, inscricaoId));
        assertTrue(chamou[0]);

        TestConnectionHolder holder = new TestConnectionHolder();
        Conexao.setConexaoCustomizada(holder::createProxy);

        GerenciarChamadoRequest reqAceitar = criarGerenciarChamadoRequest("ACEITAR", turmaId);
        assertDoesNotThrow(() -> service.gerenciarChamado(escolaA, inscricaoId, reqAceitar));
        assertTrue(atualizouStatus[0]);
    }

    @Test
    void umaEscolaNaoConsegueAlterarInscricoesDeOutra() {
        UUID escolaA = UUID.randomUUID();
        UUID escolaB = UUID.randomUUID();
        UUID inscricaoId = UUID.randomUUID();

        InscricaoDAO daoStub = new InscricaoDAO() {
            @Override
            public InscricaoEscolaResumo buscarPorIdEEscola(UUID id, UUID escolaId) {
                return null;
            }

            @Override
            public boolean chamar(UUID id, UUID escolaId) {
                return false;
            }
        };

        InscricaoService service = new InscricaoService(daoStub, new TurmaDao());

        assertThrows(InscricaoNaoEncontradaException.class, () ->
                service.chamar(escolaB, inscricaoId));

        for (String acao : List.of("RECUSAR", "DESISTIR", "REPASSAR", "DESFAZER", "ACEITAR")) {
            GerenciarChamadoRequest request = criarGerenciarChamadoRequest(acao, null);
            assertThrows(InscricaoNaoEncontradaException.class, () ->
                    service.gerenciarChamado(escolaB, inscricaoId, request));
        }
    }

    @Test
    void atualizacaoDeInscricaoNaoModificaInscricoesDeOutrasEscolas() throws SQLException {
        UUID escolaA = UUID.randomUUID();
        UUID escolaB = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();

        AlunoDetalhe detalheEscolaA = new AlunoDetalhe(
                alunoId, "Aluno Compartilhado", "123.456.789-01", LocalDate.of(2020, 1, 1),
                null, BigDecimal.ZERO, false, "SIVE-001", "Creche", "Turma A", "CRECHE", "MANHA",
                BigDecimal.ZERO, "EM_ESPERA", LocalDateTime.now(), null, List.of());

        List<UUID> escolasAtualizadas = new ArrayList<>();

        AlunoDAO alunoDaoStub = new AlunoDAO() {
            @Override
            public AlunoDetalhe buscarDetalhePorEscola(Connection conn, UUID escola, UUID aluno, boolean incluirCpf) {
                if (escolaA.equals(escola)) {
                    return detalheEscolaA;
                }
                return null;
            }

            @Override
            public UUID atualizarInscricaoEscola(Connection conn, UUID escola, UUID aluno, String vaga, String serie, String turno, BigDecimal pontuacao) {
                escolasAtualizadas.add(escola);
                return UUID.randomUUID();
            }
        };

        CriterioDAO criterioDaoStub = new CriterioDAO() {
            @Override
            public List<com.sive.model.Criterio> listarTodos() {
                return List.of();
            }
        };

        InscricaoCriterioDAO inscricaoCriterioDaoStub = new InscricaoCriterioDAO() {
            @Override
            public void substituirPorInscricao(Connection conn, UUID inscricaoId, List<com.sive.model.InscricaoCriterio> itens) {
            }
        };

        AlunoService service = new AlunoService(
                alunoDaoStub,
                new AlunoResponsavelDAO(),
                new ResponsavelDAO(),
                criterioDaoStub,
                inscricaoCriterioDaoStub,
                new CriterioService(criterioDaoStub));

        CadastrarAlunoInscricaoRequest request = new CadastrarAlunoInscricaoRequest();
        request.setTipoVaga("FUNDAMENTAL");
        request.setSerieAno("1º Ano");
        request.setTurno("INTEGRAL");

        TestConnectionHolder holder = new TestConnectionHolder();
        Conexao.setConexaoCustomizada(holder::createProxy);

        service.atualizarParaEscola(escolaA, alunoId, request);

        assertEquals(1, escolasAtualizadas.size());
        assertEquals(escolaA, escolasAtualizadas.get(0));
        assertFalse(escolasAtualizadas.contains(escolaB));
    }

    @Test
    void dadosGlobaisDeAlunosEResponsaveisSaoPreservadosConformeModeloCompartilhado() throws SQLException {
        UUID escolaA = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        boolean[] alunoAtualizado = {false};
        boolean[] responsavelAtualizado = {false};

        AlunoDetalhe detalheAluno = new AlunoDetalhe(
                alunoId, "Nome Fixo Aluno", "123.456.789-01", LocalDate.of(2020, 1, 1),
                new Endereco("12345-678", "Rua X", "1", null, "Bairro", "Cidade", "SP"),
                new BigDecimal("500.00"), false, "SIVE-001", "Creche", "Turma", "CRECHE", "MANHA",
                BigDecimal.ZERO, "EM_ESPERA", LocalDateTime.now(), null, List.of());

        AlunoDAO alunoDaoStub = new AlunoDAO() {
            @Override
            public AlunoDetalhe buscarDetalhePorEscola(Connection conn, UUID escola, UUID aluno, boolean incluirCpf) {
                return detalheAluno;
            }

            @Override
            public void atualizar(Aluno aluno, boolean atualizarCpf, Connection conn) {
                alunoAtualizado[0] = true;
            }

            @Override
            public UUID atualizarInscricaoEscola(Connection conn, UUID escola, UUID aluno, String vaga, String serie, String turno, BigDecimal pontuacao) {
                return UUID.randomUUID();
            }
        };

        CriterioDAO criterioDaoStub = new CriterioDAO() {
            @Override
            public List<com.sive.model.Criterio> listarTodos() {
                return List.of();
            }
        };

        InscricaoCriterioDAO inscricaoCriterioDaoStub = new InscricaoCriterioDAO() {
            @Override
            public void substituirPorInscricao(Connection conn, UUID inscricaoId, List<com.sive.model.InscricaoCriterio> itens) {
            }
        };

        AlunoService alunoService = new AlunoService(
                alunoDaoStub,
                new AlunoResponsavelDAO(),
                new ResponsavelDAO(),
                criterioDaoStub,
                inscricaoCriterioDaoStub,
                new CriterioService(criterioDaoStub));

        CadastrarAlunoInscricaoRequest request = new CadastrarAlunoInscricaoRequest();
        request.setNome("Tentativa De Sobrescrever Nome");
        request.setTipoVaga("CRECHE");
        request.setSerieAno("Maternal");
        request.setTurno("TARDE");

        TestConnectionHolder holder = new TestConnectionHolder();
        Conexao.setConexaoCustomizada(holder::createProxy);

        alunoService.atualizarParaEscola(escolaA, alunoId, request);

        assertFalse(alunoAtualizado[0]);

        ResponsavelDAO responsavelDaoStub = new ResponsavelDAO() {
            @Override
            public ResponsavelDetalhe buscarDetalhePorEscola(Connection conn, UUID escola, UUID respId, boolean incluirCpf) {
                return new ResponsavelDetalhe(
                        responsavelId, "Responsavel Global", "123.456.789-00", "1199999999",
                        List.of(new ResponsavelDetalhe.FilhoVinculadoResumo(alunoId, "Filho", "123", LocalDate.now(), "Creche", "T", "EM_ESPERA", BigDecimal.ZERO, "MAE", true)));
            }

            @Override
            public ResponsavelDetalhe buscarDetalhePorEscola(UUID escola, UUID respId, boolean incluirCpf) {
                return buscarDetalhePorEscola(null, escola, respId, incluirCpf);
            }

            @Override
            public void atualizar(Responsavel resp, boolean atualizarCpf, Connection conn) {
                responsavelAtualizado[0] = true;
            }
        };

        ResponsavelService responsavelService = new ResponsavelService(responsavelDaoStub);

        Responsavel dadosResp = new Responsavel();
        dadosResp.setNome("Novo Nome Responsavel");
        dadosResp.setTelefone("1188888888");

        responsavelService.atualizarParaEscola(escolaA, responsavelId, dadosResp);
        assertTrue(responsavelAtualizado[0]);
    }

    @Test
    void vinculosGlobaisDeResponsaveisNaoSaoRemovidosIndevidamente() throws SQLException {
        UUID escolaA = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();

        boolean[] removerResponsaveisChamado = {false};

        AlunoDetalhe detalheAluno = new AlunoDetalhe(
                alunoId, "Aluno Com Dois Responsaveis", "123.456.789-01", LocalDate.of(2020, 1, 1),
                null, BigDecimal.ZERO, false, "SIVE-001", "Creche", "Turma", "CRECHE", "MANHA",
                BigDecimal.ZERO, "EM_ESPERA", LocalDateTime.now(), null,
                List.of(
                        new AlunoDetalhe.ResponsavelVinculadoDetalhe(UUID.randomUUID(), "Mae", "111", "999", "MAE", true),
                        new AlunoDetalhe.ResponsavelVinculadoDetalhe(UUID.randomUUID(), "Pai", "222", "888", "PAI", false)));

        AlunoDAO alunoDaoStub = new AlunoDAO() {
            @Override
            public AlunoDetalhe buscarDetalhePorEscola(Connection conn, UUID escola, UUID aluno, boolean incluirCpf) {
                return detalheAluno;
            }

            @Override
            public void removerResponsaveis(UUID aluno, Connection conn) {
                removerResponsaveisChamado[0] = true;
            }

            @Override
            public UUID atualizarInscricaoEscola(Connection conn, UUID escola, UUID aluno, String vaga, String serie, String turno, BigDecimal pontuacao) {
                return UUID.randomUUID();
            }
        };

        CriterioDAO criterioDaoStub = new CriterioDAO() {
            @Override
            public List<com.sive.model.Criterio> listarTodos() {
                return List.of();
            }
        };

        InscricaoCriterioDAO inscricaoCriterioDaoStub = new InscricaoCriterioDAO() {
            @Override
            public void substituirPorInscricao(Connection conn, UUID inscricaoId, List<com.sive.model.InscricaoCriterio> itens) {
            }
        };

        AlunoService alunoService = new AlunoService(
                alunoDaoStub,
                new AlunoResponsavelDAO(),
                new ResponsavelDAO(),
                criterioDaoStub,
                inscricaoCriterioDaoStub,
                new CriterioService(criterioDaoStub));

        CadastrarAlunoInscricaoRequest request = new CadastrarAlunoInscricaoRequest();
        request.setTipoVaga("CRECHE");
        request.setSerieAno("Berçário");
        request.setTurno("INTEGRAL");

        TestConnectionHolder holder = new TestConnectionHolder();
        Conexao.setConexaoCustomizada(holder::createProxy);

        alunoService.atualizarParaEscola(escolaA, alunoId, request);

        assertFalse(removerResponsaveisChamado[0]);
        assertTrue(holder.committed);
        assertFalse(holder.rolledBack);
    }

    @Test
    void falhasDuranteOperacoesTransacionaisProvocamRollback() throws SQLException {
        UUID escolaA = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        AlunoDetalhe detalheAluno = new AlunoDetalhe(
                alunoId, "Aluno Teste", "123.456.789-01", LocalDate.of(2020, 1, 1),
                null, BigDecimal.ZERO, false, "SIVE-001", "Creche", "Turma", "CRECHE", "MANHA",
                BigDecimal.ZERO, "EM_ESPERA", LocalDateTime.now(), null, List.of());

        AlunoDAO alunoDaoStub = new AlunoDAO() {
            @Override
            public AlunoDetalhe buscarDetalhePorEscola(Connection conn, UUID escola, UUID aluno, boolean incluirCpf) {
                return detalheAluno;
            }

            @Override
            public UUID atualizarInscricaoEscola(Connection conn, UUID escola, UUID aluno, String vaga, String serie, String turno, BigDecimal pontuacao) {
                return null;
            }
        };

        CriterioDAO criterioDaoStub = new CriterioDAO() {
            @Override
            public List<com.sive.model.Criterio> listarTodos() {
                return List.of();
            }
        };

        AlunoService alunoService = new AlunoService(
                alunoDaoStub,
                new AlunoResponsavelDAO(),
                new ResponsavelDAO(),
                criterioDaoStub,
                new InscricaoCriterioDAO(),
                new CriterioService(criterioDaoStub));

        CadastrarAlunoInscricaoRequest request = new CadastrarAlunoInscricaoRequest();
        request.setTipoVaga("CRECHE");
        request.setSerieAno("Berçário");
        request.setTurno("INTEGRAL");

        TestConnectionHolder holderAluno = new TestConnectionHolder();
        Conexao.setConexaoCustomizada(holderAluno::createProxy);

        assertThrows(IllegalArgumentException.class, () ->
                alunoService.atualizarParaEscola(escolaA, alunoId, request));

        assertTrue(holderAluno.rolledBack);
        assertFalse(holderAluno.committed);

        ResponsavelDAO responsavelDaoStub = new ResponsavelDAO() {
            @Override
            public ResponsavelDetalhe buscarDetalhePorEscola(Connection conn, UUID escola, UUID respId, boolean incluirCpf) {
                return new ResponsavelDetalhe(
                        responsavelId, "Responsavel Teste", "123", "999",
                        List.of(new ResponsavelDetalhe.FilhoVinculadoResumo(alunoId, "Filho", "123", LocalDate.now(), "Creche", "T", "EM_ESPERA", BigDecimal.ZERO, "MAE", true)));
            }

            @Override
            public void atualizar(Responsavel resp, boolean atualizarCpf, Connection conn) throws SQLException {
                throw new SQLException("Simulando falha de integridade no banco de dados");
            }
        };

        ResponsavelService responsavelService = new ResponsavelService(responsavelDaoStub);

        Responsavel dadosResp = new Responsavel();
        dadosResp.setNome("Responsavel Teste");
        dadosResp.setTelefone("1199999999");

        TestConnectionHolder holderResp = new TestConnectionHolder();
        Conexao.setConexaoCustomizada(holderResp::createProxy);

        assertThrows(SQLException.class, () ->
                responsavelService.atualizarParaEscola(escolaA, responsavelId, dadosResp));

        assertTrue(holderResp.rolledBack);
        assertFalse(holderResp.committed);
    }

    @Test
    void consultaDeResponsaveisRespeitaContextoDaEscola() throws SQLException {
        TestConnectionHolder holder = new TestConnectionHolder();
        Conexao.setConexaoCustomizada(holder::createProxy);

        UUID escolaA = UUID.randomUUID();
        UUID escolaB = UUID.randomUUID();
        UUID responsavelEscolaA = UUID.randomUUID();
        UUID responsavelEscolaB = UUID.randomUUID();

        ResponsavelResumo resumoA = new ResponsavelResumo(responsavelEscolaA, "Mae Escola A", "111", "999", 1);
        ResponsavelResumo resumoB = new ResponsavelResumo(responsavelEscolaB, "Mae Escola B", "222", "888", 1);

        ResponsavelDAO daoStub = new ResponsavelDAO() {
            @Override
            public List<ResponsavelResumo> listarPorEscola(UUID escola, String busca, Boolean comFilhos) {
                if (escolaA.equals(escola)) {
                    return List.of(resumoA);
                }
                return List.of(resumoB);
            }

            @Override
            public ResponsavelDetalhe buscarDetalhePorEscola(UUID escola, UUID respId, boolean incluirCpf) {
                if (escolaA.equals(escola) && responsavelEscolaA.equals(respId)) {
                    return new ResponsavelDetalhe(responsavelEscolaA, "Mae Escola A", "111", "999", List.of());
                }
                return null;
            }

            @Override
            public ResponsavelDetalhe buscarDetalhePorEscola(Connection conn, UUID escola, UUID respId, boolean incluirCpf) {
                return buscarDetalhePorEscola(escola, respId, incluirCpf);
            }
        };

        ResponsavelService service = new ResponsavelService(daoStub);

        List<ResponsavelResumo> listaEscolaA = service.listarPorEscola(escolaA, null, null);
        assertEquals(1, listaEscolaA.size());
        assertEquals(responsavelEscolaA, listaEscolaA.get(0).getId());

        assertThrows(IllegalArgumentException.class, () ->
                service.buscarDetalhePorEscola(escolaA, responsavelEscolaB));

        Responsavel dados = new Responsavel();
        dados.setNome("Nome");
        dados.setTelefone("119999");
        assertThrows(IllegalArgumentException.class, () ->
                service.atualizarParaEscola(escolaA, responsavelEscolaB, dados));
    }

    @Test
    void funcionalidadesExistentesContinuamFuncionando() throws SQLException {
        UUID escolaA = UUID.randomUUID();
        UUID inscricaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        InscricaoEscolaResumo item = new InscricaoEscolaResumo(
                inscricaoId, UUID.randomUUID(), "Aluno A", LocalDate.of(2020, 5, 10),
                turmaId, "Berçário", "CRECHE", "INTEGRAL", "SIVE-100", new BigDecimal("50.00"),
                StatusInscricao.EM_ESPERA, LocalDateTime.now(), null, 1);

        InscricaoDAO inscricaoDaoStub = new InscricaoDAO() {
            @Override
            public List<InscricaoEscolaResumo> listarPorEscola(UUID escolaId, StatusInscricao status, String busca, String serieAno) {
                return List.of(item);
            }

            @Override
            public Map<StatusInscricao, Integer> contarPorStatus(UUID escolaId) {
                return Map.of(StatusInscricao.EM_ESPERA, 1, StatusInscricao.CHAMADO, 0, StatusInscricao.MATRICULADO, 0);
            }
        };

        InscricaoService service = new InscricaoService(inscricaoDaoStub, new TurmaDao());

        ListaInscricoesEscolaResponse response = service.listarPorEscola(escolaA, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getItens().size());
        assertEquals(1, response.getTotalEmEspera());
        assertEquals(0, response.getTotalAguardandoMatricula());
        assertEquals(0, response.getTotalMatriculados());
    }

    private GerenciarChamadoRequest criarGerenciarChamadoRequest(String acao, UUID turmaId) {
        GerenciarChamadoRequest request = new GerenciarChamadoRequest();
        request.setAcao(acao);
        request.setTurmaId(turmaId);
        return request;
    }

    static class TestConnectionHolder {
        boolean autoCommit = true;
        boolean committed = false;
        boolean rolledBack = false;
        List<String> executedSql = new ArrayList<>();
        Map<Integer, Object> boundParameters = new HashMap<>();
        int updateCount = 1;

        Connection createProxy() {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("setAutoCommit".equals(name)) {
                            autoCommit = (Boolean) args[0];
                            return null;
                        }
                        if ("commit".equals(name)) {
                            committed = true;
                            return null;
                        }
                        if ("rollback".equals(name)) {
                            rolledBack = true;
                            return null;
                        }
                        if ("close".equals(name) || "isClosed".equals(name)) {
                            return false;
                        }
                        if ("prepareStatement".equals(name)) {
                            String sql = (String) args[0];
                            executedSql.add(sql);
                            return createPreparedStatementProxy();
                        }
                        return defaultValue(method.getReturnType());
                    });
        }

        PreparedStatement createPreparedStatementProxy() {
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class<?>[]{PreparedStatement.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("executeUpdate".equals(name)) {
                            return updateCount;
                        }
                        if ("executeQuery".equals(name)) {
                            return createResultSetProxy();
                        }
                        if ("setObject".equals(name) || "setString".equals(name) || "setBigDecimal".equals(name)) {
                            boundParameters.put((Integer) args[0], args[1]);
                            return null;
                        }
                        if ("close".equals(name) || "isClosed".equals(name)) {
                            return false;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }

        ResultSet createResultSetProxy() {
            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class},
                    (proxy, method, args) -> defaultValue(method.getReturnType()));
        }

        private Object defaultValue(Class<?> returnType) {
            if (returnType == boolean.class) return false;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == double.class) return 0.0;
            return null;
        }
    }
}
