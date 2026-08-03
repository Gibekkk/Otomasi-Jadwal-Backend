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
public class RegisterDTO {
    private String name;
    private String email;
    private String password;
    private String fcmToken;


    public void checkDTO() {
        trim();
        if(this.name == null) throw new IllegalArgumentException("Name Cannot Be NULL");
        if(this.email == null) throw new IllegalArgumentException("Email Cannot Be NULL");
        if(this.password == null) throw new IllegalArgumentException("Password Cannot Be NULL");
        if(this.fcmToken == null) throw new IllegalArgumentException("FCM Token Cannot Be NULL");
    }

    public void checkLength() {
        boolean email = Optional.ofNullable(this.email)
                .map(s -> s.length() <= 50 && s.matches("^[a-zA-Z0-9. _%+-]+@[a-zA-Z0-9. -]+\\.[a-zA-Z]{2,}$"))
                .orElse(true);
        
        if (!email)
            throw new IllegalArgumentException("Email Invalid or Exceeded Max Length");
        boolean name = Optional.ofNullable(this.name)
                .map(s -> s.length() <= 100)
                .orElse(true);
        if (!name)
            throw new IllegalArgumentException("Name Exceeded Max Length");
    }

    public void trim() {
        this.name = Optional.ofNullable(this.name).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.email = Optional.ofNullable(this.email).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.password = Optional.ofNullable(this.password).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.fcmToken = Optional.ofNullable(this.fcmToken).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}

