package com.jadwal.restfulapi.dto;

import lombok.Setter;

import java.util.Optional;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private String name;
    private int sksCount;
    private int lecturerCount;
    private int capacity;
    private Boolean isInterdiscipline;
    private Boolean isOdd;
    private Boolean isLab;
    private String categoryId;
    private String subMajorId;
    private List<String> specializations;

    public void checkDTO(Boolean isProdiAdmin) {
        trim();
        checkLength();
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
        if (this.categoryId == null && !isProdiAdmin)
            throw new IllegalArgumentException("Category ID Cannot Be NULL");
        if (this.sksCount == 0)
            throw new IllegalArgumentException("SKS Count Cannot Be 0");
        if (this.lecturerCount == 0)
            throw new IllegalArgumentException("Lecturer Count Cannot Be 0");
        if (this.capacity < 5 || this.capacity > 300)
            throw new IllegalArgumentException("Capacity Must Be Between 5 And 300");
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
        this.subMajorId = Optional.ofNullable(this.subMajorId).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
