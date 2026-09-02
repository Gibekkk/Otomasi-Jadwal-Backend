package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.SubMajor;

public interface SubMajorRepository extends JpaRepository<SubMajor, String> {
    public List<SubMajor> findAllByCategoryIdAndDeletedAtIsNull(Category categoryId);
    public Boolean existsByNameAndCategoryIdAndDeletedAtIsNull(String name, Category categoryId);
    public Boolean existsByNameAndCategoryIdAndDeletedAtIsNullAndIdNot(String name, Category categoryId, String id);
    public Optional<SubMajor> findByIdAndCategoryIdAndDeletedAtIsNull(String id, Category categoryId);
}
