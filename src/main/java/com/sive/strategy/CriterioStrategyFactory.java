package com.sive.strategy;

import com.sive.model.Aluno;

public final class CriterioStrategyFactory {

    private CriterioStrategyFactory() {}

    public static CriterioPontuacaoStrategy obterEstrategia(String codigoCriterio) {
        if (codigoCriterio == null || codigoCriterio.trim().isEmpty()) {
            return new CriterioSemRegraAutomaticaStrategy();
        }
        return switch (codigoCriterio.trim().toUpperCase()) {
            case "RENDA" -> new CriterioRendaStrategy();
            case "PCD" -> new CriterioBooleanoStrategy(
                    "Aluno com deficiência ou necessidade especial (PcD)",
                    Aluno::isTemNecessidadeEspecial);
            case "IRMAO" -> new CriterioBooleanoStrategy(
                    "Irmão matriculado na mesma escola",
                    Aluno::isIrmaoMatriculado);
            case "VULNERABILIDADE" -> new CriterioBooleanoStrategy(
                    "Vulnerabilidade socioeconômica",
                    Aluno::isVulnerabilidadeSocioeconomica);
            case "BPC_LOAS" -> new CriterioBooleanoStrategy(
                    "Beneficiário do BPC/LOAS",
                    Aluno::isBpcLoas);
            case "BOLSA_FAMILIA" -> new CriterioBooleanoStrategy(
                    "Beneficiário do Bolsa Família",
                    Aluno::isBolsaFamilia);
            case "RESPONSAVEL_SOLO" -> new CriterioBooleanoStrategy(
                    "Responsável solo",
                    Aluno::isResponsavelSolo);
            case "MORA_ALUGUEL" -> new CriterioBooleanoStrategy(
                    "Mora de aluguel",
                    Aluno::isMoraDeAluguel);
            default -> new CriterioSemRegraAutomaticaStrategy();
        };
    }
}
