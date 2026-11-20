package com.sive.model;

public enum StatusInscricao {
    EM_ESPERA,
    CHAMADO,
    MATRICULADO,
    CANCELADO,
    EXPIRADO;

    public static StatusInscricao fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return StatusInscricao.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean podeTransicionarPara(StatusInscricao novoStatus) {
        if (novoStatus == null) {
            return false;
        }
        return switch (this) {
            case EM_ESPERA -> novoStatus == CHAMADO || novoStatus == CANCELADO || novoStatus == EM_ESPERA;
            case CHAMADO -> novoStatus == MATRICULADO || novoStatus == EXPIRADO || novoStatus == CANCELADO;
            case EXPIRADO -> novoStatus == EM_ESPERA;
            case MATRICULADO -> novoStatus == CANCELADO;
            case CANCELADO -> false;
        };
    }
}
