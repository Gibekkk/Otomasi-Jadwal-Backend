package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Boolean isProdiExistById(String prodiId) {
        return categoryRepository.findByIdAndIsProdiTrueAndDeletedAtIsNull(prodiId).isPresent();
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
}