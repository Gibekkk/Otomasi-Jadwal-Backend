package com.jadwal.restfulapi.model.enums;

public enum Role {
    PRODI("Prodi Admin"),
    BAA("BAA Admin"),
    PM("PM Admin"),
    NTHUM("NTHUM Admin"),
    SUPERADMIN("Super Admin");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String toString() {
        return role;
    }

    public static boolean checkExist(String role) {
        for (Role s : Role.values()) {
            if (s.role.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public static Role fromString(String role) {
        for (Role s : Role.values()) {
            if (s.role.equalsIgnoreCase(role)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Role Unknown: " + role);
    }
}