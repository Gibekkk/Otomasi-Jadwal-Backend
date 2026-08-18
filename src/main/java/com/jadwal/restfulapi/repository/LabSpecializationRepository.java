package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.jadwal.restfulapi.model.LabSpecialization;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.LabGroup;

public interface LabSpecializationRepository extends JpaRepository<LabSpecialization, String> {
    @Transactional
    public void deleteAllByLabGroupId(LabGroup labGroup);

    @Transactional
    public void deleteAllBySpecializationId(Specialization specializationId);
}