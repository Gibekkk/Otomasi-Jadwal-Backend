package com.jadwal.restfulapi.dto;

import lombok.Setter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDTO {

    private int academicYear;
    private Boolean isOdd;

}
