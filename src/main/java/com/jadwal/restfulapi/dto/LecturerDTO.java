package com.jadwal.restfulapi.dto;

import lombok.Setter;

import java.util.Optional;
import java.util.List;
import com.jadwal.restfulapi.model.enums.Religion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LecturerDTO {

    private String name;
    private Boolean isMale;
    private Boolean isInterdicipline;
    private String religion;
    private String categoryId;
    private List<String> specializations;
    private List<String> schedules;

    public void checkDTO() {
        trim();
        checkLength();
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
        if (this.categoryId == null)
            throw new IllegalArgumentException("Category ID Cannot Be NULL");
        if (this.religion == null)
            throw new IllegalArgumentException("Religion Cannot Be NULL");
        if (this.religion != null && !Religion.checkExist(this.religion)) {
            throw new IllegalArgumentException("Invalid Religion: " + this.religion);
        }
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
        this.categoryId = Optional.ofNullable(this.categoryId).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.religion = Optional.ofNullable(this.religion).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
