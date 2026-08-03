package com.jadwal.restfulapi.model.enums;

import java.util.Arrays;
import java.util.List;

public enum Mood {
    SAD("Sad"),
    ANGRY("Angry"),
    HAPPY("Happy"),
    NEUTRAL("Neutral");

    private final String mood;

    Mood(String mood) {
        this.mood = mood;
    }

    public static List<String> getMoods() {
        return Arrays.stream(Mood.values())
                .map(m -> m.mood)
                .toList();
    }

    public static List<Mood> getMoodList() {
        return Arrays.stream(Mood.values())
                .toList();
    }

    public String toString() {
        return mood;
    }

    public static boolean checkExist(String mood) {
        for (Mood s : Mood.values()) {
            if (s.mood.equalsIgnoreCase(mood)) {
                return true;
            }
        }
        return false;
    }

    public static Mood fromString(String mood) {
        for (Mood s : Mood.values()) {
            if (s.mood.equalsIgnoreCase(mood)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Mood Unknown: " + mood);
    }
}