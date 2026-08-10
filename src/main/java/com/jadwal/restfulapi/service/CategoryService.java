package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.dto.NameDTO;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Boolean isProdiExistById(String prodiId) {
        return findProdiById(prodiId).isPresent();
    }

    public Boolean isCategoryExistById(String categoryId) {
        return findCategoryById(categoryId).isPresent();
    }

    public Optional<Category> findCategoryById(String id) {
        return categoryRepository.findByIdAndIsProdiFalseAndDeletedAtIsNull(id);
    }

    public Optional<Category> findProdiById(String id) {
        return categoryRepository.findByIdAndIsProdiTrueAndDeletedAtIsNull(id);
    }

    public List<Category> findAllCategory() {
        return categoryRepository.findAllByIsProdiFalseAndDeletedAtIsNull();
    }

    public List<Category> findAllProdi() {
        return categoryRepository.findAllByIsProdiTrueAndDeletedAtIsNull();
    }

    public void deleteCategory(Category category) {
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    public Category createCategory(NameDTO categoryDTO, User user) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setIsProdi(false);
        category.setCreatedBy(user);
        category.setEditedBy(user);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public Category editCategory(Category editedCategory, NameDTO categoryDTO, User user) {
        editedCategory.setName(categoryDTO.getName());
        editedCategory.setIsProdi(false);
        editedCategory.setCreatedBy(user);
        editedCategory.setEditedBy(user);
        editedCategory.setCreatedAt(LocalDateTime.now());
        editedCategory.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(editedCategory);
    }

    public Category createProdi(NameDTO categoryDTO, User user) {
        Category prodi = new Category();
        prodi.setName(categoryDTO.getName());
        prodi.setIsProdi(true);
        prodi.setCreatedBy(user);
        prodi.setEditedBy(user);
        prodi.setCreatedAt(LocalDateTime.now());
        prodi.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(prodi);
    }

    public Category editProdi(Category editedProdi, NameDTO categoryDTO, User user) {
        editedProdi.setName(categoryDTO.getName());
        editedProdi.setIsProdi(true);
        editedProdi.setCreatedBy(user);
        editedProdi.setEditedBy(user);
        editedProdi.setCreatedAt(LocalDateTime.now());
        editedProdi.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(editedProdi);
    }
}