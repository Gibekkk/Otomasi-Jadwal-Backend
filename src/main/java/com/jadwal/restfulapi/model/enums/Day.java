package com.jadwal.restfulapi.model.enums;

public enum Day {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday");

    private final String day;

    Day(String day) {
        this.day = day;
    }

    public String toString() {
        return day;
    }

    public static boolean checkExist(String day) {
        for (Day s : Day.values()) {
            if (s.day.equalsIgnoreCase(day)) {
                return true;
            }
        }
        return false;
    }

    public static Day fromString(String day) {
        for (Day s : Day.values()) {
            if (s.day.equalsIgnoreCase(day)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Day Unknown: " + day);
    }
}