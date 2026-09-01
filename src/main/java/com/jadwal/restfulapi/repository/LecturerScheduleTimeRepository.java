package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.jadwal.restfulapi.model.LecturerSchedule;
import com.jadwal.restfulapi.model.LecturerScheduleTime;

public interface LecturerScheduleTimeRepository extends JpaRepository<LecturerScheduleTime, String> {
    public List<LecturerScheduleTime> findAllByLecturerScheduleId(LecturerSchedule lecturerSchedule);

    @Transactional
    public void deleteAllByLecturerScheduleId(LecturerSchedule lecturerSchedule);
}
