package com.moodbites.restfulapi.model.enums;

public enum SampleFood {
    NASI_GORENG_MERAH("Nasi Goreng Merah"),
    MIE_GORENG_JAKARTA("Mie Goreng Jakarta"),
    MIE_KERING_TITI("Mie Kering / Titi"),
    PAKET_AYAM_GEPREK("Paket Ayam Geprek"),
    PAKET_TELUR_GIMBAL("Paket Telur Gimbal"),
    BAKSO_KUAH("Bakso Kuah"),
    BEEF_TERIYAKI("Beef Teriyaki"),
    BEEF_PATTY("Beef Patty"),
    KENTANG_GORENG("Kentang Goreng"),
    UBI_GORENG("Ubi Goreng"),
    ANEKA_INDOMIE("Aneka Indomie"),
    NASI_AYAM_PANGGANG("Nasi Ayam Panggang"),
    NASI_AYAM_LENGKUAS("Nasi Ayam Lengkuas"),
    MIE_NYEMEK("Mie Nyemek"),
    MIE_KERING("Mie Kering"),
    MIE_BAKSO("Mie Bakso"),
    MIE_GORENG_JAWA("Mie Goreng Jawa"),
    NASI_GILA("Nasi Gila"),
    YAMIEN("Yamien"),
    TAHU_CRISPY("Tahu Crispy"),
    PISANG_GORENG("Pisang Goreng");

    private final String sampleFood;

    SampleFood(String sampleFood) {
        this.sampleFood = sampleFood;
    }

    public String toString() {
        return sampleFood;
    }

    public static boolean checkExist(String sampleFood) {
        for (SampleFood s : SampleFood.values()) {
            if (s.sampleFood.equalsIgnoreCase(sampleFood)) {
                return true;
            }
        }
        return false;
    }

    public static SampleFood fromString(String sampleFood) {
        for (SampleFood s : SampleFood.values()) {
            if (s.sampleFood.equalsIgnoreCase(sampleFood)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Sample Food Unknown: " + sampleFood);
    }
}