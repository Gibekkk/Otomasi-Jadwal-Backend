package com.moodbites.restfulapi.dto;

import lombok.Setter;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private String name;

    public boolean checkDTO() {
        trim();
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
        return name != null & checkLength();
    }

    public boolean checkLength() {
        boolean name = Optional.ofNullable(this.name)
                .map(s -> s.length() <= 100)
                .orElse(true);
        if (!name)
            throw new IllegalArgumentException("Name Exceeded Max Length");
        return name;
    }

    public void trim() {
        this.name = Optional.ofNullable(this.name).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
