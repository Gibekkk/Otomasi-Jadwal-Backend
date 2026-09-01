package com.jadwal.restfulapi.dto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jadwal.restfulapi.model.enums.Day;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LecturerScheduleDTO {
    private String day;
    private List<String> scheduleIds;

    public void checkDTO() {
        trim();
        if (this.day == null)
            throw new IllegalArgumentException("Day Cannot Be NULL");
        if (this.day != null && !Day.checkExist(this.day))
            throw new IllegalArgumentException("Invalid Day: " + this.day);
        if (this.scheduleIds == null)
            throw new IllegalArgumentException("Schedule IDs Cannot Be NULL");
    }

    public void trim() {
        this.day = Optional.ofNullable(this.day).map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        this.scheduleIds = Optional.ofNullable(this.scheduleIds).map(List::stream).orElseGet(Stream::empty).map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toList());
    }
}
