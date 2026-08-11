package com.jadwal.restfulapi.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    public boolean existsByTimeStart(LocalTime timeStart);
    public boolean existsByTimeEnd(LocalTime timeEnd);
    public Optional<Schedule> findByTimeStart(LocalTime timeStart);
    public Optional<Schedule> findByTimeEnd(LocalTime timeEnd);
    public List<Schedule> findAllByIdIn(List<String> scheduleIds);
}