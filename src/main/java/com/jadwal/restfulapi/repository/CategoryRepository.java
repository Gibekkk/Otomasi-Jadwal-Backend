package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Category;

public interface CategoryRepository extends JpaRepository<Category, String> {
    public Optional<Category> findByIdAndDeletedAtIsNull(String id);
    public List<Category> findAllByDeletedAtIsNull();
    public List<Category> findAllByIsProdiTrueAndDeletedAtIsNull();
    public List<Category> findAllByIsProdiFalseAndDeletedAtIsNull();
    public Optional<Category> findByIdAndIsProdiTrueAndDeletedAtIsNull(String id);
    public Optional<Category> findByIdAndIsProdiFalseAndDeletedAtIsNull(String id);
    public Boolean existsByNameAndDeletedAtIsNull(String name);
    public Boolean existsByNameAndDeletedAtIsNullAndIdIsNot(String name, String id);
}
