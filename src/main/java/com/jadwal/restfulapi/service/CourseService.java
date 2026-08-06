package com.jadwal.restfulapi.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.dto.CourseDTO;
import com.jadwal.restfulapi.repository.CourseRepository;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public Optional<Course> findCourseById(String id) {
        return courseRepository.findByIdAndDeletedAtIsNull(id);
    }

    public List<Course> findCourseByCategory(Category category) {
        return courseRepository.findAllByCategoryIdAndDeletedAtIsNull(category);
    }

    public List<Course> findAllCourse() {
        return courseRepository.findAllByDeletedAtIsNull();
    }

    public void deleteCourse(Course course) {
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    public Course createCourse(CourseDTO courseDTO, Category category, User admin) {
        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setSksCount(courseDTO.getSksCount());
        course.setCategoryId(category);
        course.setCreatedBy(admin);
        course.setEditedBy(admin);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    public Course editCourse(Course editedCourse, CourseDTO courseDTO, Category category, User admin) {
        editedCourse.setName(courseDTO.getName());
        editedCourse.setSksCount(courseDTO.getSksCount());
        editedCourse.setCategoryId(category);
        editedCourse.setEditedBy(admin);
        editedCourse.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(editedCourse);
    }
}