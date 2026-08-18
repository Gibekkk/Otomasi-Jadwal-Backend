package com.jadwal.restfulapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.LecturerSpecialization;
import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.Specialization;

public interface LecturerSpecializationRepository extends JpaRepository<LecturerSpecialization, String> {
    public List<LecturerSpecialization> findAllByLecturerId(Lecturer lecturerId);
    public void deleteAllByLecturerId(Lecturer lecturerId);
    public void deleteAllBySpecializationId(Specialization specializationId);
}
