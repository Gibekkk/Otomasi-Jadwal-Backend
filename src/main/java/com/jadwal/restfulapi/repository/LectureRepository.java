package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, String> {
    public Optional<Lecture> findByIdAndDeletedAtIsNull(String id);
    public List<Lecture> findAllByDeletedAtIsNull();
    public List<Lecture> findAllByLecturerIdIdAndDeletedAtIsNull(String lecturerId);
    public List<Lecture> findAllByCourseScheduleIdIdAndDeletedAtIsNull(String courseScheduleId);
}
