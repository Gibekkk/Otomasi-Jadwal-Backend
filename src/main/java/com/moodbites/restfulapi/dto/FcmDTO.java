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
public class FcmDTO {
    private String fcmToken;

    public boolean checkDTO() {
        trim();
        if(this.fcmToken == null) throw new IllegalArgumentException("FCM Token Cannot Be NULL");
        return fcmToken != null;
    }

    public void trim() {
        this.fcmToken = Optional.ofNullable(this.fcmToken).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}
