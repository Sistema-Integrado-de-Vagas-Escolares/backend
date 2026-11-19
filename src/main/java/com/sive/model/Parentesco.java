package com.sive.model;

public enum Parentesco {

    MAE,
    PAI,
    AVOS,
    TUTOR_LEGAL,
    OUTRO;

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
