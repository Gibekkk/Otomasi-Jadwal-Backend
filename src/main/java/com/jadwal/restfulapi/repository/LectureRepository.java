package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.CourseSchedule;
import com.jadwal.restfulapi.model.Lecture;
import com.jadwal.restfulapi.model.Lecturer;

public interface LectureRepository extends JpaRepository<Lecture, String> {
    public List<Lecture> findAllByLecturerId(Lecturer lecturerId);
    public List<Lecture> findAllByCourseScheduleId(CourseSchedule courseScheduleId);
}
