package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.dto.CategoryDTO;
import com.jadwal.restfulapi.dto.SubMajorDTO;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.SubMajor;
import com.jadwal.restfulapi.repository.CategoryRepository;
import com.jadwal.restfulapi.repository.LecturerRepository;
import com.jadwal.restfulapi.repository.SubMajorRepository;
import com.jadwal.restfulapi.repository.CourseRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private SubMajorRepository subMajorRepository;

    @Autowired
    private CourseRepository courseRepository;

    public Boolean isProdiExistById(String prodiId) {
        return findProdiById(prodiId).isPresent();
    }

    public Boolean isCategoryExistById(String categoryId) {
        return findCategoryById(categoryId).isPresent();
    }

    public Boolean isCategoryExistByName(String name) {
        return categoryRepository.existsByNameAndDeletedAtIsNull(name);
    }

    public Boolean isCategoryExistByNameAndIdIsNot(String name, String id) {
        return categoryRepository.existsByNameAndDeletedAtIsNullAndIdIsNot(name, id);
    }

    public Optional<Category> findCategoryById(String id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<Category> findProdiById(String id) {
        return categoryRepository.findByIdAndIsProdiTrueAndDeletedAtIsNull(id);
    }

    public List<Category> findAllCategory() {
        return categoryRepository.findAllByDeletedAtIsNull();
    }

    public List<Category> findAllProdi() {
        return categoryRepository.findAllByIsProdiTrueAndDeletedAtIsNull();
    }

    public void deleteCategory(Category category) {
        category.setDeletedAt(LocalDateTime.now());
        Category deletedCategory = categoryRepository.save(category);

        for(Lecturer lecturer : deletedCategory.getCategoryLecturers()) {
            lecturer.setDeletedAt(LocalDateTime.now());
            lecturerRepository.save(lecturer);
        }
    }

    public void deleteSubMajor(SubMajor subMajor) {
        subMajor.setDeletedAt(LocalDateTime.now());
        SubMajor savedSubMajor = subMajorRepository.save(subMajor);

        for(Course course : savedSubMajor.getSubMajorCourses()) {
            course.setSubMajorId(null);
            courseRepository.save(course);
        }
    }

    public Category createCategory(CategoryDTO categoryDTO, User user) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setIsProdi(false);
        category.setCreatedBy(user);
        category.setEditedBy(user);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public Category editCategory(Category editedCategory, CategoryDTO categoryDTO, User user) {
        editedCategory.setName(categoryDTO.getName());
        editedCategory.setIsProdi(false);
        editedCategory.setEditedBy(user);
        editedCategory.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(editedCategory);
    }

    public Category createProdi(CategoryDTO categoryDTO, User user) {
        Category prodi = new Category();
        prodi.setName(categoryDTO.getName());
        prodi.setIsProdi(true);
        prodi.setCreatedBy(user);
        prodi.setEditedBy(user);
        prodi.setCreatedAt(LocalDateTime.now());
        prodi.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(prodi);
    }

    public Category editProdi(Category editedProdi, CategoryDTO categoryDTO, User user) {
        editedProdi.setName(categoryDTO.getName());
        editedProdi.setIsProdi(true);
        editedProdi.setEditedBy(user);
        editedProdi.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(editedProdi);
    }

    public Boolean isSubMajorExistByNameAndCategory(String name, Category category) {
        return subMajorRepository.existsByNameAndCategoryIdAndDeletedAtIsNull(name, category);
    }

    public Boolean isSubMajorExistByNameAndCategoryAndIdNot(String name, Category category, String id) {
        return subMajorRepository.existsByNameAndCategoryIdAndDeletedAtIsNullAndIdNot(name, category, id);
    }

    public List<SubMajor> findAllSubMajorsByCategory(Category category) {
        return subMajorRepository.findAllByCategoryIdAndDeletedAtIsNull(category);
    }

    public Optional<SubMajor> findSubMajorByIdAndCategory(String subMajorId, Category category) {
        return subMajorRepository.findByIdAndCategoryIdAndDeletedAtIsNull(subMajorId, category);
    }

    public SubMajor createSubMajor(SubMajorDTO subMajorDTO, Category category, User user) {
        SubMajor subMajor = new SubMajor();
        subMajor.setName(subMajorDTO.getName());
        subMajor.setCategoryId(category);
        subMajor.setCreatedBy(user);
        subMajor.setEditedBy(user);
        subMajor.setCreatedAt(LocalDateTime.now());
        subMajor.setUpdatedAt(LocalDateTime.now());
        return subMajorRepository.save(subMajor);
    }

    public SubMajor editSubMajor(SubMajor subMajor, SubMajorDTO subMajorDTO, Category category, User user) {
        subMajor.setName(subMajorDTO.getName());
        subMajor.setCategoryId(category);
        subMajor.setEditedBy(user);
        subMajor.setUpdatedAt(LocalDateTime.now());
        return subMajorRepository.save(subMajor);
    }
}
