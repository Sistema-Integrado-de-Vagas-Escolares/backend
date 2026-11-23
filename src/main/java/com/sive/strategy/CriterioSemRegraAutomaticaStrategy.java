package com.sive.strategy;

import com.sive.model.Inscricao;

public class CriterioSemRegraAutomaticaStrategy implements CriterioPontuacaoStrategy {

    @Override
    public double calcularPontos(Inscricao inscricao, double pontosBase) {
        return 0.0;
    }

    @Override
    public String getNomeCriterio() {
        return "Critério customizado";
    }
}
