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
public class LoginDTO {
    private String username;
    private String password;
    public void checkDTO() {
        trim();
        checkLength();
        if (this.username == null)
            throw new IllegalArgumentException("Username Cannot Be NULL");
        if (this.password == null)
            throw new IllegalArgumentException("Password Cannot Be NULL");
    }

    public void checkLength() {
        boolean username = Optional.ofNullable(this.username)
                .map(s -> s.length() <= 50)
                .orElse(true);

        if (!username)
            throw new IllegalArgumentException("Username Exceeded Max Length");
    }

    public void trim() {
        this.username = Optional.ofNullable(this.username).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.password = Optional.ofNullable(this.password).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
