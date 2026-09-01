package com.jadwal.restfulapi.wrapper;

import java.util.List;
import com.jadwal.restfulapi.model.Schedule;
import com.jadwal.restfulapi.model.enums.Day;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LecturerScheduleWrapper {
    private Day day;
    private List<Schedule> schedules;
}
