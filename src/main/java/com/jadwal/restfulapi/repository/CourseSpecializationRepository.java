package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.CourseSpecialization;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.Course;

public interface CourseSpecializationRepository extends JpaRepository<CourseSpecialization, String> {
    public List<CourseSpecialization> findAllByCourseId(Course courseId);
    public void deleteAllByCourseId(Course courseId);
    public void deleteAllBySpecializationId(Specialization specializationId);
}
