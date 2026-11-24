package com.sive.service;

import com.sive.dao.Conexao;
import com.sive.dao.CriterioDAO;
import com.sive.dao.InscricaoCriterioDAO;
import com.sive.dao.InscricaoDAO;
import com.sive.model.Aluno;
import com.sive.model.Criterio;
import com.sive.model.Inscricao;
import com.sive.model.InscricaoCriterio;
import com.sive.strategy.CriterioPontuacaoStrategy;
import com.sive.strategy.CriterioStrategyFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CriterioService {

    private static final Set<String> TIPOS_RESPOSTA_VALIDOS = Set.of("FAIXA_VALORES", "SIM_NAO", "DISTANCIA");
    private static final Set<String> CATEGORIAS_VALIDAS = Set.of("SOCIAL", "GEOGRAFICO", "FAMILIAR");

    private final CriterioDAO criterioDAO;
    private final InscricaoDAO inscricaoDAO;
    private final InscricaoCriterioDAO inscricaoCriterioDAO;

    public CriterioService(CriterioDAO criterioDAO) {
        this.criterioDAO = criterioDAO;
        this.inscricaoDAO = new InscricaoDAO();
        this.inscricaoCriterioDAO = new InscricaoCriterioDAO();
    }

    public static class ResultadoPontuacao {
        private final BigDecimal pontuacaoTotal;
        private final List<InscricaoCriterio> itens;

        public ResultadoPontuacao(BigDecimal pontuacaoTotal, List<InscricaoCriterio> itens) {
            this.pontuacaoTotal = pontuacaoTotal;
            this.itens = itens;
        }

        public BigDecimal getPontuacaoTotal() {
            return pontuacaoTotal;
        }

        public List<InscricaoCriterio> getItens() {
            return itens;
        }
    }

    public List<Criterio> listarCriterios() throws SQLException {
        return criterioDAO.listarTodos();
    }



public List<Criterio> publicarCriterios(List<Criterio> criterios) throws SQLException {
    if (criterios == null || criterios.isEmpty()) {
        throw new IllegalArgumentException("Informe ao menos um critério de pontuação.");
    }

    try (Connection conn = Conexao.getConnection()) {
        conn.setAutoCommit(false);

        try {
            List<Criterio> atuais = criterioDAO.listarTodos(conn);
            Map<UUID, String> codigosPorId = new HashMap<>();

            for (Criterio atual : atuais) {
                if (atual.getId() != null && atual.getCodigo() != null) {
                    codigosPorId.put(atual.getId(), atual.getCodigo());
                }
            }

            Set<String> nomesVistos = new HashSet<>();
            double somaPesosAtivos = 0.0;

            for (Criterio criterio : criterios) {
                if (criterio.getNome() == null || criterio.getNome().trim().isEmpty()) {
                    throw new IllegalArgumentException("Todos os critérios devem possuir um nome.");
                }

                String nomeNormalizado = criterio.getNome().trim();

                if (!nomesVistos.add(nomeNormalizado.toLowerCase())) {
                    throw new IllegalArgumentException("Já existe um critério com o nome: " + nomeNormalizado);
                }

                criterio.setNome(nomeNormalizado);
                criterio.setDescricao(criterio.getDescricao() != null ? criterio.getDescricao().trim() : "");

                if (criterio.getCodigo() == null || criterio.getCodigo().trim().isEmpty()) {
                    String codigoExistente = criterio.getId() != null ? codigosPorId.get(criterio.getId()) : null;
                    criterio.setCodigo(codigoExistente != null ? codigoExistente : inferirCodigoPorNome(nomeNormalizado));
                } else {
                    criterio.setCodigo(criterio.getCodigo().trim().toUpperCase());
                }

                if (criterio.getPontosBase() < 0 || criterio.getPontosBase() > 100) {
                    throw new IllegalArgumentException("O peso do critério deve estar entre 0% e 100%.");
                }

                String tipoResposta = criterio.getTipoResposta() != null
                        ? criterio.getTipoResposta().trim().toUpperCase()
                        : "SIM_NAO";

                if (!TIPOS_RESPOSTA_VALIDOS.contains(tipoResposta)) {
                    throw new IllegalArgumentException("Tipo de resposta inválido: " + tipoResposta);
                }

                criterio.setTipoResposta(tipoResposta);

                String categoria = criterio.getCategoria() != null
                        ? criterio.getCategoria().trim().toUpperCase()
                        : ("DISTANCIA".equals(tipoResposta) ? "GEOGRAFICO" : "SOCIAL");

                if (!CATEGORIAS_VALIDAS.contains(categoria)) {
                    categoria = "SOCIAL";
                }

                criterio.setCategoria(categoria);

                if (criterio.isAtivo()) {
                    somaPesosAtivos += criterio.getPontosBase();
                }
            }

            if (Math.abs(somaPesosAtivos - 100.0) > 0.01) {
                throw new IllegalArgumentException(
                        "A soma dos pesos dos critérios ativos deve ser exatamente 100%. Soma atual: "
                                + Math.round(somaPesosAtivos) + "%.");
            }

            List<Criterio> publicados = criterioDAO.substituirTodos(conn, criterios);
            recalcularInscricoesEmEspera(conn, publicados);

            conn.commit();
            return publicados;

        } catch (SQLException | RuntimeException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}



    public ResultadoPontuacao avaliarCriterios(Aluno aluno, List<Criterio> criterios) {
        Inscricao inscricaoTemp = new Inscricao();
        inscricaoTemp.setAluno(aluno);

        List<InscricaoCriterio> itens = new ArrayList<>();
        double total = 0.0;

        if (criterios != null) {
            for (Criterio criterio : criterios) {
                if (!criterio.isAtivo() || criterio.getId() == null) {
                    continue;
                }
                CriterioPontuacaoStrategy strategy = CriterioStrategyFactory.obterEstrategia(criterio.getCodigo());
                double pontos = strategy.calcularPontos(inscricaoTemp, criterio.getPontosBase());
                if (pontos > 0.0) {
                    itens.add(new InscricaoCriterio(null, criterio.getId(), pontos));
                    total += pontos;
                }
            }
        }

        BigDecimal pontuacaoTotal = BigDecimal.valueOf(Math.min(100.0, total))
                .setScale(2, RoundingMode.HALF_UP);
        return new ResultadoPontuacao(pontuacaoTotal, itens);
    }



    private void recalcularInscricoesEmEspera(
            Connection conn,
            List<Criterio> criteriosPublicados) throws SQLException {

        List<Inscricao> inscricoes = inscricaoDAO.listarInscricoesEmEsperaParaRecalculo(conn);

        for (Inscricao inscricao : inscricoes) {
            ResultadoPontuacao resultado = avaliarCriterios(inscricao.getAluno(), criteriosPublicados);

            inscricaoCriterioDAO.substituirPorInscricao(
                    conn,
                    inscricao.getId(),
                    resultado.getItens());

            inscricaoDAO.atualizarPontuacaoTotal(
                    conn,
                    inscricao.getId(),
                    resultado.getPontuacaoTotal());
        }
    }



    private String inferirCodigoPorNome(String nome) {
        String lower = nome.toLowerCase();
        if (lower.contains("renda")) return "RENDA";
        if (lower.contains("pcd") || lower.contains("defici")) return "PCD";
        if (lower.contains("irmão") || lower.contains("irmao")) return "IRMAO";
        if (lower.contains("vulnerabilidade")) return "VULNERABILIDADE";
        if (lower.contains("bpc") || lower.contains("loas")) return "BPC_LOAS";
        if (lower.contains("bolsa fam")) return "BOLSA_FAMILIA";
        if (lower.contains("solo") || lower.contains("solteir")) return "RESPONSAVEL_SOLO";
        if (lower.contains("aluguel")) return "MORA_ALUGUEL";
        if (lower.contains("proximidade")) return "PROXIMIDADE";
        return null;
    }
}
