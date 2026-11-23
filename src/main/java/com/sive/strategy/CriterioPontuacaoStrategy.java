package com.sive.strategy;

import com.sive.model.Inscricao;

public interface CriterioPontuacaoStrategy {

    double calcularPontos(Inscricao inscricao, double pontosBase);

    default double calcularPontos(Inscricao inscricao) {
        return calcularPontos(inscricao, 0.0);
    }

    String getNomeCriterio();
}
