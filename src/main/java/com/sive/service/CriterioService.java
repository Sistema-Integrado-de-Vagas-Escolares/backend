package com.sive.service;

import com.sive.dao.CriterioDAO;
import com.sive.model.Criterio;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Regras de negócio para gerenciamento dos critérios de pontuação da fila.
 */
@Service
public class CriterioService {

    private static final Set<String> TIPOS_RESPOSTA_VALIDOS = Set.of("FAIXA_VALORES", "SIM_NAO", "DISTANCIA");
    private static final Set<String> CATEGORIAS_VALIDAS = Set.of("SOCIAL", "GEOGRAFICO", "FAMILIAR");

    private final CriterioDAO criterioDAO;

    public CriterioService(CriterioDAO criterioDAO) {
        this.criterioDAO = criterioDAO;
    }

    public List<Criterio> listarCriterios() throws SQLException {
        return criterioDAO.listarTodos();
    }

    public List<Criterio> publicarCriterios(List<Criterio> criterios) throws SQLException {
        if (criterios == null || criterios.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um critério de pontuação.");
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

        return criterioDAO.substituirTodos(criterios);
    }
}
