package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {
}
