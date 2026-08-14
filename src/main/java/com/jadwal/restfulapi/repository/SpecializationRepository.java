package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, String> {
    public Optional<Specialization> findByIdAndDeletedAtIsNull(String id);
    public List<Specialization> findAllByDeletedAtIsNull();
    public Optional<Specialization> findByNameAndDeletedAtIsNull(String name);
    public List<Specialization> findAllByIdInAndDeletedAtIsNull(List<String> specializationIds);
    public Boolean existsByNameAndDeletedAtIsNull(String name);
}
