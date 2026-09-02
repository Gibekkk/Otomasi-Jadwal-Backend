package com.jadwal.restfulapi.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.model.CourseSpecialization;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.dto.CourseDTO;
import com.jadwal.restfulapi.repository.CourseRepository;
import com.jadwal.restfulapi.repository.CourseSpecializationRepository;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSpecializationRepository courseSpecializationRepository;

    private Boolean isCategoryActive(Course course) {
        return course.getCategoryId().getDeletedAt() == null;
    }

    public Boolean isCourseExistByName(String name) {
        return findCourseByName(name).isPresent();
    }

    public Boolean isCourseExistByNameAndIdIsNot(String name, String id) {
        return findCourseByName(name).filter(c -> !c.getId().equals(id)).isPresent();
    }

    public Boolean isCourseExistById(String id) {
        return findCourseById(id).isPresent();
    }

    public Optional<Course> findCourseById(String id) {
        return courseRepository.findByIdAndDeletedAtIsNull(id)
                .filter(this::isCategoryActive);
    }

    public Optional<Course> findCourseByName(String name) {
        return courseRepository.findByNameAndDeletedAtIsNull(name)
                .filter(this::isCategoryActive);
    }

    public List<Course> findCourseByCategory(Category category) {
        return courseRepository.findAllByCategoryIdAndDeletedAtIsNull(category)
                .stream()
                .filter(this::isCategoryActive)
                .toList();
    }

    public Optional<Course> findCourseByIdAndCategory(String id, Category category) {
        return courseRepository.findByIdAndDeletedAtIsNull(id)
                .filter(course -> course.getCategoryId().equals(category))
                .filter(this::isCategoryActive);
    }

    public List<Course> findAllCourse() {
        return courseRepository.findAllByDeletedAtIsNull()
                .stream()
                .filter(this::isCategoryActive)
                .toList();
    }

    public Optional<Course> findCourseByIdAndCategoryAndInterdiscipline(String id, Category category) {
        // Otomatis terfilter karena memanggil findCourseByIdAndCategory yang sudah kita update
        return findCourseByIdAndCategory(id, category)
                .filter(course -> course.getIsInterdiscipline());
    }

    public List<Course> findCourseByCategoryAndInterdiscipline(Category category) {
        // Otomatis terfilter karena memanggil findAllCourse yang sudah kita update
        return findAllCourse()
                .stream()
                .filter(course -> course.getIsInterdiscipline() || course.getCategoryId().equals(category))
                .toList();
    }

    public void deleteCourse(Course course) {
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    public Boolean makeCourseActive(Course course) {
        course.setIsActive(true);
        courseRepository.save(course);
        return course.getIsActive();
    }

    public Boolean makeCourseInactive(Course course) {
        course.setIsActive(false);
        courseRepository.save(course);
        return course.getIsActive();
    }

    public Course createCourse(CourseDTO courseDTO, Category category, User admin, List<Specialization> specializations) {
        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setSksCount(courseDTO.getSksCount());
        course.setLecturerCount(courseDTO.getLecturerCount());
        course.setCapacity(courseDTO.getCapacity());
        course.setSubMajorId(courseDTO.getSubMajorId());
        course.setIsInterdiscipline(courseDTO.getIsInterdiscipline());
        course.setIsOdd(courseDTO.getIsOdd());
        course.setIsActive(true);
        course.setIsLab(courseDTO.getIsLab());
        course.setCategoryId(category);
        course.setCreatedBy(admin);
        course.setEditedBy(admin);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        Course savedCourse = courseRepository.save(course);

        for (Specialization specialization : specializations) {
            courseSpecializationRepository.save(new CourseSpecialization(null, savedCourse, specialization));
        }

        return savedCourse;
    }

    public Course editCourse(Course editedCourse, CourseDTO courseDTO, Category category, User admin, List<Specialization> specializations) {
        editedCourse.setName(courseDTO.getName());
        editedCourse.setSksCount(courseDTO.getSksCount());
        editedCourse.setLecturerCount(courseDTO.getLecturerCount());
        editedCourse.setCapacity(courseDTO.getCapacity());
        editedCourse.setSubMajorId(courseDTO.getSubMajorId());
        editedCourse.setIsInterdiscipline(courseDTO.getIsInterdiscipline());
        editedCourse.setIsOdd(courseDTO.getIsOdd());
        editedCourse.setIsLab(courseDTO.getIsLab());
        editedCourse.setCategoryId(category);
        editedCourse.setEditedBy(admin);
        editedCourse.setUpdatedAt(LocalDateTime.now());
        Course savedCourse = courseRepository.save(editedCourse);

        deleteCourseSpecializationsByCourse(savedCourse);
        for (Specialization specialization : specializations) {
            courseSpecializationRepository.save(new CourseSpecialization(null, savedCourse, specialization));
        }

        return savedCourse;
    }

    public void deleteCourseSpecializationsByCourse(Course course) {
        courseSpecializationRepository.deleteAllByCourseId(course);
    }
}
