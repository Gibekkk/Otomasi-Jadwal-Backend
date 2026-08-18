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
public class RoomDTO {

    private String name;
    private String labGroupId;
    private int capacity;

    public void checkDTO() {
        trim();
        checkLength();
        if (this.name == null)
            throw new IllegalArgumentException("Name Cannot Be NULL");
    }

    public void checkLength() {
        boolean name = Optional.ofNullable(this.name)
                .map(s -> s.length() <= 50)
                .orElse(true);
        boolean capacity = this.capacity >= 5;

        if (!name)
            throw new IllegalArgumentException("Name Exceeded Max Length");
        if (!capacity)
            throw new IllegalArgumentException("Capacity Must Be At Least 5");
    }

    public void trim() {
        this.name = Optional.ofNullable(this.name).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.labGroupId = Optional.ofNullable(this.labGroupId).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
    }

}


