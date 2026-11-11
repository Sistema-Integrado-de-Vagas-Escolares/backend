package com.sive.model;

public enum Turno {
    MANHA, TARDE, INTEGRAL;

    public static Turno fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Turno.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}