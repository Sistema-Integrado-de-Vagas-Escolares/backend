package com.sive.strategy;

import com.sive.model.Inscricao;

public interface CriterioPontuacaoStrategy {

    double calcularPontos(Inscricao inscricao);

    String getNomeCriterio();
}
