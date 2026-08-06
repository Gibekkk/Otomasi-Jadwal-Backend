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
public class UserDTO {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserGroupService userGroupService;
   
    private String name;
    private String groupId;
    private String prodiId;
    private String username;
    private String password;

    public void checkDTO() {
        trim();
        checkLength();
        if (this.username == null)
            throw new IllegalArgumentException("Username Cannot Be NULL");
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
        if (this.groupId == null)
            throw new IllegalArgumentException("Group ID Cannot Be NULL");
        if (this.password == null)
            throw new IllegalArgumentException("Password Cannot Be NULL");
        if (this.prodiId != null && !categoryService.isProdiExistById(this.prodiId))
            throw new IllegalArgumentException("Prodi ID Not Found");
        if (!userGroupService.isProdiExistById(this.groupId))
            throw new IllegalArgumentException("Group ID Not Found");
    }

    public void checkLength() {
        boolean username = Optional.ofNullable(this.username)
                .map(s -> s.length() <= 50)
                .orElse(true);

        if (!username)
            throw new IllegalArgumentException("Username Exceeded Max Length");
    }

    public void trim() {
        this.name = Optional.ofNullable(this.name).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.groupId = Optional.ofNullable(this.groupId).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.prodiId = Optional.ofNullable(this.prodiId).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.username = Optional.ofNullable(this.username).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.password = Optional.ofNullable(this.password).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
