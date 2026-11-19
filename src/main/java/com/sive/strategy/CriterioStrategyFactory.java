package com.sive.strategy;

public final class CriterioStrategyFactory {

    private CriterioStrategyFactory() {}

    public static CriterioPontuacaoStrategy obterEstrategia(String codigoCriterio) {
        if (codigoCriterio == null) {
            throw new IllegalArgumentException("Código do critério não pode ser nulo.");
        }
        return switch (codigoCriterio.toUpperCase()) {
            case "PCD" -> new CriterioPcdStrategy();
            case "RENDA", "PROXIMIDADE", "IRMAO" -> throw new UnsupportedOperationException("Critério pendente de definição de regras: " + codigoCriterio);
            default -> throw new IllegalArgumentException("Critério desconhecido: " + codigoCriterio);
        };
    }
}
