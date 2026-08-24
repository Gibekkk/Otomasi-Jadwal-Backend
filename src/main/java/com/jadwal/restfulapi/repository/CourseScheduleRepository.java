package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.CourseSchedule;
import com.jadwal.restfulapi.model.Room;
import com.jadwal.restfulapi.model.Schedule;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, String> {
    public List<CourseSchedule> findAllByScheduleId(Schedule scheduleId);
    public List<CourseSchedule> findAllByRoomId(Room roomId);
}
