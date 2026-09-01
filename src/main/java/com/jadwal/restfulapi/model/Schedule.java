package com.jadwal.restfulapi.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedules")
public class Schedule {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    @OneToMany(mappedBy = "scheduleId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CourseSchedule> courseSchedules;

    @OneToMany(mappedBy = "scheduleId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LecturerScheduleTime> lecturerScheduleTimes;

}
