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
public class EmailDTO {
    private String email;

    public boolean checkDTO() {
        trim();
        if (this.email == null)
            throw new IllegalArgumentException("Email Cannot Be NULL");
        return this.email != null && checkLength();
    }

    public boolean checkLength() {
        boolean email = Optional.ofNullable(this.email)
                .map(s -> s.length() <= 50 && s.matches("^[a-zA-Z0-9. _%+-]+@[a-zA-Z0-9. -]+\\.[a-zA-Z]{2,}$"))
                .orElse(true);
        
        if (!email)
            throw new IllegalArgumentException("Email Invalid or Exceeded Max Length");

        return email;
    }

    public void trim() {
        this.email = Optional.ofNullable(this.email).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }
}
