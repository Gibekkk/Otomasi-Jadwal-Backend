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
public class VerifyOTPDTO {
    private String loginId;
    private String code;


    public void checkDTO() {
        trim();
        if(this.loginId == null) throw new IllegalArgumentException("Login ID Cannot Be NULL");
        if(this.code == null) throw new IllegalArgumentException("OTP Code Cannot Be NULL");
    }

    public void trim() {
        this.loginId = Optional.ofNullable(this.loginId).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.code = Optional.ofNullable(this.code).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}

