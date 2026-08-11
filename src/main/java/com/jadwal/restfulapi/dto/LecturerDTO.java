package com.jadwal.restfulapi.dto;

import lombok.Setter;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadwal.restfulapi.service.CategoryService;
import com.jadwal.restfulapi.model.enums.Religion;
import com.jadwal.restfulapi.service.SpecializationService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LecturerDTO {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpecializationService specializationService;

    private String name;
    private Boolean isDlb;
    private Boolean isMale;
    private Boolean isInterdicipline;
    private String religion;
    private String categoryId;
    private List<String> specializations;

    public void checkDTO() {
        trim();
        checkLength();
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
        if (this.categoryId == null)
            throw new IllegalArgumentException("Category ID Cannot Be NULL");
        if (this.religion == null)
            throw new IllegalArgumentException("Religion Cannot Be NULL");
        if (!categoryService.isCategoryExistById(this.categoryId))
            throw new IllegalArgumentException("Category ID Not Found");
        if (this.specializations != null && !this.specializations.isEmpty()) {
            List<String> nonExistentSpecializations = specializationService.checkNonExistentSpecializations(this.specializations);
            if (!nonExistentSpecializations.isEmpty()) {
                throw new IllegalArgumentException("Specialization IDs Not Found: " + String.join(", ", nonExistentSpecializations));
            }
        }
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
