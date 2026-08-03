package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.LecturerSpecialization;

public interface LecturerSpecializationRepository extends JpaRepository<LecturerSpecialization, String> {
    public Optional<LecturerSpecialization> findByIdAndDeletedAtIsNull(String id);
    public List<LecturerSpecialization> findAllByDeletedAtIsNull();
    public List<LecturerSpecialization> findAllByLecturerIdIdAndDeletedAtIsNull(String lecturerId);
}
