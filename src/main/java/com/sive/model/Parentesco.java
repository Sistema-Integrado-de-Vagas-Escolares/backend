package com.sive.model;

/**
 * Enumeracao que representa os possiveis graus de parentesco
 * entre um Aluno e um Responsavel Legal.
 *
 * <p>Os valores devem ser identicos aos definidos na constraint
 * {@code CHECK} da coluna {@code parentesco} na tabela
 * {@code aluno_responsaveis} do banco de dados.</p>
 */
public enum Parentesco {

    MAE,
    PAI,
    AVOS,
    TUTOR_LEGAL,
    OUTRO;

    /**
     * Converte uma String para o enum correspondente, retornando {@code null}
     * caso o valor seja invalido ou nulo.
     *
     * @param value String a ser convertida.
     * @return O enum correspondente ou {@code null}.
     */
    public static Parentesco fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Parentesco.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

