package com.sive.strategy;

import com.sive.model.Inscricao;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CriterioRendaStrategy implements CriterioPontuacaoStrategy {

    private static final BigDecimal FAIXA_BAIXA = new BigDecimal("706.00");
    private static final BigDecimal FAIXA_MEDIA = new BigDecimal("1412.00");
    private static final BigDecimal FAIXA_ALTA = new BigDecimal("2118.00");

    @Override
    public double calcularPontos(Inscricao inscricao, double pontosBase) {
        if (inscricao == null || inscricao.getAluno() == null || pontosBase <= 0) {
            return 0.0;
        }
        BigDecimal renda = inscricao.getAluno().getRendaPerCapita();
        if (renda == null) {
            return 0.0;
        }

        double fator;
        if (renda.compareTo(FAIXA_BAIXA) <= 0) {
            fator = 1.0;
        } else if (renda.compareTo(FAIXA_MEDIA) <= 0) {
            fator = 0.60;
        } else if (renda.compareTo(FAIXA_ALTA) <= 0) {
            fator = 0.32;
        } else {
            return 0.0;
        }

        return BigDecimal.valueOf(pontosBase * fator)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public String getNomeCriterio() {
        return "Renda familiar per capita";
    }
}
