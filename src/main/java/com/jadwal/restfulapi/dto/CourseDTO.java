package com.jadwal.restfulapi.dto;

import lombok.Setter;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadwal.restfulapi.service.CategoryService;
import com.jadwal.restfulapi.service.UserGroupService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    @Autowired
    private CategoryService categoryService;

    private String name;
    private int sksCount;
    private String categoryId;

    public void checkDTO() {
        trim();
        checkLength();
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
        if (this.categoryId == null)
            throw new IllegalArgumentException("Category ID Cannot Be NULL");
        if (this.sksCount == 0)
            throw new IllegalArgumentException("SKS Count Cannot Be NULL");
        if (!categoryService.isCategoryExistById(this.categoryId))
            throw new IllegalArgumentException("Category ID Not Found");
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
    }

}
