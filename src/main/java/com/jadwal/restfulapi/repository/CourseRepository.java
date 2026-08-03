package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Course;

public interface CourseRepository extends JpaRepository<Course, String> {
    public Optional<Course> findByIdAndDeletedAtIsNull(String id);
    public List<Course> findAllByDeletedAtIsNull();
    public List<Course> findAllByCategoryIdIdAndDeletedAtIsNull(String categoryId);
}
