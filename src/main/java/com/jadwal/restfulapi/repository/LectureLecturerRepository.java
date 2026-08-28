package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.LectureLecturer;
import com.jadwal.restfulapi.model.Lecture;

public interface LectureLecturerRepository extends JpaRepository<LectureLecturer, String> {
    public List<LectureLecturer> findAllByLecturerId(Lecturer lecturerId);
    public List<LectureLecturer> findAllByLectureId(Lecture lectureId);

    @Transactional
    public void deleteAllByLecturerId(Lecturer lecturerId);

    @Transactional
    public void deleteAllByLectureId(Lecture lectureId);
}
