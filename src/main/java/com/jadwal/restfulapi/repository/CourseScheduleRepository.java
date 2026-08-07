package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.CourseSchedule;
import com.jadwal.restfulapi.model.Room;
import com.jadwal.restfulapi.model.Schedule;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, String> {
    public Optional<CourseSchedule> findByIdAndDeletedAtIsNull(String id);
    public List<CourseSchedule> findAllByDeletedAtIsNull();
    public List<CourseSchedule> findAllByScheduleIdAndDeletedAtIsNull(Schedule scheduleId);
    public List<CourseSchedule> findAllByRoomIdAndDeletedAtIsNull(Room roomId);
}
