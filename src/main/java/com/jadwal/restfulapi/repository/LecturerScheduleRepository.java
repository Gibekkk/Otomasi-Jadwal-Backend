package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.LecturerSchedule;
import com.jadwal.restfulapi.model.Schedule;

public interface LecturerScheduleRepository extends JpaRepository<LecturerSchedule, String> {
    public List<LecturerSchedule> findAllByLecturerId(Lecturer lecturerId);
    public List<LecturerSchedule> findAllByScheduleId(Schedule scheduleId);
    public void deleteAllByLecturerId(Lecturer lecturerId);
}
