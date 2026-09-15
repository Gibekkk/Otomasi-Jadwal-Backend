package com.jadwal.restfulapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jadwal.restfulapi.dto.CourseDTO;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.SubMajor;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.repository.CourseRepository;
import com.jadwal.restfulapi.repository.CourseSpecializationRepository;

/**
 * Unit tests for {@link CourseService}, focused on the "active category"
 * filtering rules and the create/edit flows.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSpecializationRepository courseSpecializationRepository;

    @InjectMocks
    private CourseService courseService;

    private User admin;
    private Category activeCategory;
    private Category deletedCategory;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId("admin-id");

        activeCategory = new Category();
        activeCategory.setId("cat-active");
        activeCategory.setDeletedAt(null);

        deletedCategory = new Category();
        deletedCategory.setId("cat-deleted");
        deletedCategory.setDeletedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("findCourseById returns the course when its category is still active")
    void findCourseByIdReturnsCourseWithActiveCategory() {
        Course course = new Course();
        course.setId("course-1");
        course.setCategoryId(activeCategory);
        when(courseRepository.findByIdAndDeletedAtIsNull("course-1")).thenReturn(Optional.of(course));

        Optional<Course> result = courseService.findCourseById("course-1");

        assertThat(result).contains(course);
    }

    @Test
    @DisplayName("findCourseById hides the course when its parent category has been soft deleted")
    void findCourseByIdHidesCourseWithDeletedCategory() {
        Course course = new Course();
        course.setId("course-2");
        course.setCategoryId(deletedCategory);
        when(courseRepository.findByIdAndDeletedAtIsNull("course-2")).thenReturn(Optional.of(course));

        Optional<Course> result = courseService.findCourseById("course-2");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllCourse filters out courses whose category is inactive")
    void findAllCourseFiltersInactiveCategoryCourses() {
        Course activeCourse = new Course();
        activeCourse.setCategoryId(activeCategory);
        Course orphanCourse = new Course();
        orphanCourse.setCategoryId(deletedCategory);

        when(courseRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(activeCourse, orphanCourse));

        List<Course> result = courseService.findAllCourse();

        assertThat(result).containsExactly(activeCourse);
    }

    @Test
    @DisplayName("isCourseExistByNameAndIdIsNot is true only when a different course shares the name")
    void isCourseExistByNameAndIdIsNotExcludesSameId() {
        Course course = new Course();
        course.setId("course-1");
        course.setCategoryId(activeCategory);
        when(courseRepository.findByNameAndDeletedAtIsNull("Algoritma")).thenReturn(Optional.of(course));

        assertThat(courseService.isCourseExistByNameAndIdIsNot("Algoritma", "course-1")).isFalse();
        assertThat(courseService.isCourseExistByNameAndIdIsNot("Algoritma", "some-other-id")).isTrue();
    }

    @Test
    @DisplayName("findCourseByCategoryAndInterdiscipline includes interdiscipline courses from other categories")
    void findCourseByCategoryAndInterdisciplineIncludesCrossCategoryInterdisciplineCourses() {
        Category otherCategory = new Category();
        otherCategory.setId("cat-other");
        otherCategory.setDeletedAt(null);

        Course sameCategoryCourse = new Course();
        sameCategoryCourse.setCategoryId(activeCategory);
        sameCategoryCourse.setIsInterdiscipline(false);

        Course interdisciplineFromOther = new Course();
        interdisciplineFromOther.setCategoryId(otherCategory);
        interdisciplineFromOther.setIsInterdiscipline(true);

        Course nonInterdisciplineFromOther = new Course();
        nonInterdisciplineFromOther.setCategoryId(otherCategory);
        nonInterdisciplineFromOther.setIsInterdiscipline(false);

        when(courseRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(sameCategoryCourse, interdisciplineFromOther, nonInterdisciplineFromOther));

        List<Course> result = courseService.findCourseByCategoryAndInterdiscipline(activeCategory);

        assertThat(result).containsExactlyInAnyOrder(sameCategoryCourse, interdisciplineFromOther);
    }

    @Test
    @DisplayName("createCourse saves the course and one CourseSpecialization per specialization")
    void createCourseSavesSpecializations() {
        CourseDTO dto = new CourseDTO();
        dto.setName("Struktur Data");
        dto.setSksCount(3);
        dto.setLecturerCount(1);
        dto.setCapacity(40);
        dto.setIsInterdiscipline(false);
        dto.setIsOdd(true);
        dto.setIsLab(false);

        Specialization specialization = new Specialization();
        specialization.setId("spec-1");

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course result = courseService.createCourse(dto, activeCategory, admin, List.of(specialization),
                Optional.<SubMajor>empty());

        assertThat(result.getName()).isEqualTo("Struktur Data");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCategoryId()).isEqualTo(activeCategory);
        assertThat(result.getSubMajorId()).isNull();
        verify(courseSpecializationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("makeCourseActive and makeCourseInactive flip the isActive flag and return its new value")
    void toggleActiveFlag() {
        Course course = new Course();
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(courseService.makeCourseActive(course)).isTrue();
        assertThat(courseService.makeCourseInactive(course)).isFalse();
    }
}
