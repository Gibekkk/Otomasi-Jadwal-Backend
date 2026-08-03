package com.jadwal.restfulapi.model.enums;

public enum Religion {
    ISLAM("Islam"),
    CHRISTIAN("Christian"),
    CATHOLIC("Catholic"),
    HINDUISM("Hinduism"),
    BUDDHISM("Buddhism"),
    CONFUCIANISM("Confucianism");

    private final String religion;

    Religion(String religion) {
        this.religion = religion;
    }

    public String toString() {
        return religion;
    }

    public static boolean checkExist(String religion) {
        for (Religion s : Religion.values()) {
            if (s.religion.equalsIgnoreCase(religion)) {
                return true;
            }
        }
        return false;
    }

    public static Religion fromString(String religion) {
        for (Religion s : Religion.values()) {
            if (s.religion.equalsIgnoreCase(religion)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Religion Unknown: " + religion);
    }
}