package com.moodbites.restfulapi.model.enums;

import java.util.Arrays;
import java.util.List;

public enum Flavor {
    SWEET("Manis"),
    SPICY("Pedas"),
    SALTY("Asin / Gurih"),
    BITTER("Pahit"),
    SOUR("Asam / Segar");

    private final String flavor;

    Flavor(String flavor) {
        this.flavor = flavor;
    }

    public String toString() {
        return flavor;
    }

    public static List<String> getFlavors() {
        return Arrays.stream(Flavor.values())
                .map(f -> f.flavor)
                .toList();
    }

    public static List<Flavor> getFlavorList() {
        return Arrays.stream(Flavor.values())
                .toList();
    }

    public static boolean checkExist(String flavor) {
        for (Flavor s : Flavor.values()) {
            if (s.flavor.equalsIgnoreCase(flavor)) {
                return true;
            }
        }
        return false;
    }

    public static Flavor fromString(String flavor) {
        for (Flavor s : Flavor.values()) {
            if (s.flavor.equalsIgnoreCase(flavor)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Flavor Unknown: " + flavor);
    }
}