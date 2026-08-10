package com.jadwal.restfulapi.dto;

import lombok.Setter;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NameDTO {
    private String name;
    public void checkDTO() {
        trim();
        checkLength();
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
    }

    public void checkLength() {
        boolean name = Optional.ofNullable(this.name)
                .map(s -> s.length() <= 50)
                .orElse(true);

        if (!name)
            throw new IllegalArgumentException("Name Exceeded Max Length");
    }

    public void trim() {
        this.name = Optional.ofNullable(this.name).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
