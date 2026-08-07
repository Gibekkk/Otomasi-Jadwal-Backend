package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.LecturerSchedule;
import com.jadwal.restfulapi.model.Schedule;

public interface LecturerScheduleRepository extends JpaRepository<LecturerSchedule, String> {
    public Optional<LecturerSchedule> findByIdAndDeletedAtIsNull(String id);
    public List<LecturerSchedule> findAllByDeletedAtIsNull();
    public List<LecturerSchedule> findAllByLecturerIdAndDeletedAtIsNull(Lecturer lecturerId);
    public List<LecturerSchedule> findAllByScheduleIdAndDeletedAtIsNull(Schedule scheduleId);
}
