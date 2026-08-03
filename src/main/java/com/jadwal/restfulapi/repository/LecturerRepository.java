package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Lecturer;

public interface LecturerRepository extends JpaRepository<Lecturer, String> {
    public Optional<Lecturer> findByIdAndDeletedAtIsNull(String id);
    public List<Lecturer> findAllByDeletedAtIsNull();
    public List<Lecturer> findAllByLecturerTypeIdIdAndDeletedAtIsNull(String lecturerTypeId);
    public List<Lecturer> findAllByProdiIdIdAndDeletedAtIsNull(String prodiId);
}
