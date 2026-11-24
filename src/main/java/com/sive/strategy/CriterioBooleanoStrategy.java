package com.sive.strategy;

import com.sive.model.Aluno;
import com.sive.model.Inscricao;

import java.util.Objects;
import java.util.function.Predicate;

public class CriterioBooleanoStrategy implements CriterioPontuacaoStrategy {

    private final String nomeCriterio;
    private final Predicate<Aluno> condicao;

    public CriterioBooleanoStrategy(String nomeCriterio, Predicate<Aluno> condicao) {
        this.nomeCriterio = Objects.requireNonNull(nomeCriterio, "Nome do critério é obrigatório.");
        this.condicao = Objects.requireNonNull(condicao, "Condição do critério é obrigatória.");
    }

    @Override
    public double calcularPontos(Inscricao inscricao, double pontosBase) {
        if (inscricao != null && inscricao.getAluno() != null && condicao.test(inscricao.getAluno())) {
            return Math.max(0.0, pontosBase);
        }
        return 0.0;
    }

    @Override
    public String getNomeCriterio() {
        return nomeCriterio;
    }
}
