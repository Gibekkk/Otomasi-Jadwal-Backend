package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.jadwal.restfulapi.model.LecturerSpecialization;
import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.Specialization;

public interface LecturerSpecializationRepository extends JpaRepository<LecturerSpecialization, String> {
    public List<LecturerSpecialization> findAllByLecturerId(Lecturer lecturerId);

    @Transactional
    public void deleteAllByLecturerId(Lecturer lecturerId);

    @Transactional
    public void deleteAllBySpecializationId(Specialization specializationId);
}
