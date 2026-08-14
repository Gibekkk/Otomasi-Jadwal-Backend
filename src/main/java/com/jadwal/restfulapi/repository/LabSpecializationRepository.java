package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.LabSpecialization;
import com.jadwal.restfulapi.model.LabGroup;

public interface LabSpecializationRepository extends JpaRepository<LabSpecialization, String> {
    public void deleteAllByLabGroupId(LabGroup labGroup);
}