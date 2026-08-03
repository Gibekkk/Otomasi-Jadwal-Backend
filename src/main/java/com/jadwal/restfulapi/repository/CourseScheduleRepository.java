package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.CourseSchedule;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, String> {
    public Optional<CourseSchedule> findByIdAndDeletedAtIsNull(String id);
    public List<CourseSchedule> findAllByDeletedAtIsNull();
    public List<CourseSchedule> findAllByScheduleIdIdAndDeletedAtIsNull(String scheduleId);
    public List<CourseSchedule> findAllByRoomIdIdAndDeletedAtIsNull(String roomId);
}
