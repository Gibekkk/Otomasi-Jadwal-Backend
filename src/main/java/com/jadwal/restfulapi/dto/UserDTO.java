package com.jadwal.restfulapi.dto;

import lombok.Setter;

import java.util.Optional;

import com.jadwal.restfulapi.model.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String name;
    private String role;
    private String prodiId;
    private String username;
    private String password;

    public void checkDTO(Boolean isRegistration) {
        trim();
        checkLength();
        if (this.username == null)
            throw new IllegalArgumentException("Username Cannot Be NULL");
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
        if (this.role == null)
            throw new IllegalArgumentException("Role Cannot Be NULL");
        if (this.password == null && isRegistration)
            throw new IllegalArgumentException("Password Cannot Be NULL");
        if (this.role != null && !Role.checkExist(this.role))
            throw new IllegalArgumentException("Invalid Role: " + this.role);
    }

    public void checkLength() {
        boolean username = Optional.ofNullable(this.username)
                .map(s -> s.length() <= 15 && s.length() >= 3)
                .orElse(true);
        boolean name = Optional.ofNullable(this.name)
                .map(s -> s.length() <= 50)
                .orElse(true);

        if (!username)
            throw new IllegalArgumentException("Username Must Be Between 3 To 15 Characters");

        if (!name)
            throw new IllegalArgumentException("Name Exceeded Max Length");
    }

    public void trim() {
        this.name = Optional.ofNullable(this.name).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.role = Optional.ofNullable(this.role).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.prodiId = Optional.ofNullable(this.prodiId).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.username = Optional.ofNullable(this.username).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.password = Optional.ofNullable(this.password).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
