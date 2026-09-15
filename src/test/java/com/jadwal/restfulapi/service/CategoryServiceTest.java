package com.jadwal.restfulapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jadwal.restfulapi.dto.CategoryDTO;
import com.jadwal.restfulapi.dto.SubMajorDTO;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.SubMajor;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.repository.CategoryRepository;
import com.jadwal.restfulapi.repository.CourseRepository;
import com.jadwal.restfulapi.repository.LecturerRepository;
import com.jadwal.restfulapi.repository.SubMajorRepository;

/**
 * Unit tests for {@link CategoryService}.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LecturerRepository lecturerRepository;

    @Mock
    private SubMajorRepository subMajorRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId("admin-id");
    }

    @Test
    @DisplayName("createCategory persists a non-prodi category with audit fields set")
    void createCategorySavesNonProdiCategory() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Wajib");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.createCategory(dto, admin);

        assertThat(result.getName()).isEqualTo("Wajib");
        assertThat(result.getIsProdi()).isFalse();
        assertThat(result.getCreatedBy()).isEqualTo(admin);
        assertThat(result.getEditedBy()).isEqualTo(admin);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createProdi persists a category flagged as prodi")
    void createProdiSavesProdiCategory() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Informatika");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.createProdi(dto, admin);

        assertThat(result.getIsProdi()).isTrue();
        assertThat(result.getName()).isEqualTo("Informatika");
    }

    @Test
    @DisplayName("editCategory updates the name and edited-by without flipping isProdi to true")
    void editCategoryUpdatesFields() {
        Category existing = new Category();
        existing.setId("cat-1");
        existing.setName("Old Name");
        existing.setIsProdi(false);

        CategoryDTO dto = new CategoryDTO();
        dto.setName("New Name");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.editCategory(existing, dto, admin);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getIsProdi()).isFalse();
        assertThat(result.getEditedBy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("deleteCategory soft deletes the category and cascades the soft delete to its lecturers")
    void deleteCategoryCascadesToLecturers() {
        Category category = new Category();
        category.setId("cat-1");

        Lecturer lecturerOne = new Lecturer();
        Lecturer lecturerTwo = new Lecturer();
        Set<Lecturer> lecturers = new HashSet<>();
        lecturers.add(lecturerOne);
        lecturers.add(lecturerTwo);
        category.setCategoryLecturers(lecturers);

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lecturerRepository.save(any(Lecturer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.deleteCategory(category);

        assertThat(category.getDeletedAt()).isNotNull();
        assertThat(lecturerOne.getDeletedAt()).isNotNull();
        assertThat(lecturerTwo.getDeletedAt()).isNotNull();
        verify(lecturerRepository, times(2)).save(any(Lecturer.class));
    }

    @Test
    @DisplayName("deleteSubMajor soft deletes the sub-major and detaches its courses")
    void deleteSubMajorDetachesCourses() {
        SubMajor subMajor = new SubMajor();
        subMajor.setId("submajor-1");

        Course course = new Course();
        course.setSubMajorId(subMajor);
        Set<Course> courses = new HashSet<>();
        courses.add(course);
        subMajor.setSubMajorCourses(courses);

        when(subMajorRepository.save(any(SubMajor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        when(courseRepository.save(courseCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.deleteSubMajor(subMajor);

        assertThat(subMajor.getDeletedAt()).isNotNull();
        assertThat(courseCaptor.getValue().getSubMajorId()).isNull();
    }

    @Test
    @DisplayName("createSubMajor links the new sub-major to its parent category")
    void createSubMajorLinksToCategory() {
        Category category = new Category();
        category.setId("cat-1");

        SubMajorDTO dto = new SubMajorDTO();
        dto.setName("RPL");
        when(subMajorRepository.save(any(SubMajor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubMajor result = categoryService.createSubMajor(dto, category, admin);

        assertThat(result.getName()).isEqualTo("RPL");
        assertThat(result.getCategoryId()).isEqualTo(category);
    }

    @Test
    @DisplayName("isCategoryExistByName delegates straight to the repository")
    void isCategoryExistByNameDelegatesToRepository() {
        when(categoryRepository.existsByNameAndDeletedAtIsNull("Wajib")).thenReturn(true);
        when(categoryRepository.existsByNameAndDeletedAtIsNull("Pilihan")).thenReturn(false);

        assertThat(categoryService.isCategoryExistByName("Wajib")).isTrue();
        assertThat(categoryService.isCategoryExistByName("Pilihan")).isFalse();
    }

    @Test
    @DisplayName("findCategoryById returns empty when the repository finds nothing")
    void findCategoryByIdReturnsEmptyWhenMissing() {
        when(categoryRepository.findByIdAndDeletedAtIsNull("missing")).thenReturn(Optional.empty());

        assertThat(categoryService.findCategoryById("missing")).isEmpty();
        verify(categoryRepository, never()).findAllByDeletedAtIsNull();
    }
}
