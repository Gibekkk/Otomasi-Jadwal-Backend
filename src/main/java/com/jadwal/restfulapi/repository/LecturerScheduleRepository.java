package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.LecturerSchedule;

public interface LecturerScheduleRepository extends JpaRepository<LecturerSchedule, String> {
    public List<LecturerSchedule> findAllByLecturerId(Lecturer lecturerId);
   
    @Transactional
    public void deleteAllByLecturerId(Lecturer lecturerId);
}
