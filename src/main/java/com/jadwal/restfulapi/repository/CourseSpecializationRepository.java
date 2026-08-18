package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.jadwal.restfulapi.model.CourseSpecialization;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.Course;

public interface CourseSpecializationRepository extends JpaRepository<CourseSpecialization, String> {
    public List<CourseSpecialization> findAllByCourseId(Course courseId);

    @Transactional
    public void deleteAllByCourseId(Course courseId);

    @Transactional
    public void deleteAllBySpecializationId(Specialization specializationId);
}
