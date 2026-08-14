package com.jadwal.restfulapi.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.LabGroup;

public interface LabGroupRepository extends JpaRepository<LabGroup, String> {
    public Boolean existsByNameAndDeletedAtIsNull(String name);
    public Optional<LabGroup> findByIdAndDeletedAtIsNull(String id);
    public Boolean existsByIdAndDeletedAtIsNull(String id);
    public List<LabGroup> findByDeletedAtIsNull();
}