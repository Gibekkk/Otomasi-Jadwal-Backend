package com.moodbites.restfulapi.dto.constructs;

import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlavorProfile {
    @JsonProperty("Manis")
    private Integer manis;

    @JsonProperty("Pedas")
    private Integer pedas;

    @JsonProperty("Asin / Gurih")
    private Integer asinGurih;

    @JsonProperty("Asam / Segar")
    private Integer asamSegar;

    @JsonProperty("Pahit")
    private Integer pahit;

    public void checkDTO(String context) {
        if (this.manis == null)
            throw new IllegalArgumentException(context + ": Manis Cannot Be NULL");
        if (this.pedas == null)
            throw new IllegalArgumentException(context + ": Pedas Cannot Be NULL");
        if (this.asinGurih == null)
            throw new IllegalArgumentException(context + ": Asin/Gurih Cannot Be NULL");
        if (this.asamSegar == null)
            throw new IllegalArgumentException(context + ": Asam/Segar Cannot Be NULL");
        if (this.pahit == null)
            throw new IllegalArgumentException(context + ": Pahit Cannot Be NULL");

        checkRange(this.manis, context + " Manis");
        checkRange(this.pedas, context + " Pedas");
        checkRange(this.asinGurih, context + " Asin/Gurih");
        checkRange(this.asamSegar, context + " Asam/Segar");
        checkRange(this.pahit, context + " Pahit");
    }

    private void checkRange(Integer value, String fieldName) {
        if (value < 1 || value > 5)
            throw new IllegalArgumentException(fieldName + ": Value Must Be Between 1-5");
    }
}
