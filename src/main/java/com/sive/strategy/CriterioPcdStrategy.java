package com.sive.strategy;

import com.sive.model.Inscricao;

public class CriterioPcdStrategy implements CriterioPontuacaoStrategy {

    private static final double PONTOS_PCD = 30.0;

    @Override
    public double calcularPontos(Inscricao inscricao) {
        if (inscricao != null && inscricao.getAluno() != null && inscricao.getAluno().isTemNecessidadeEspecial()) {
            return PONTOS_PCD;
        }
        return 0.0;
    }

    @Override
    public String getNomeCriterio() {
        return "Necessidade Especial (PcD)";
    }
}
