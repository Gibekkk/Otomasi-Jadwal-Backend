package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.LecturerType;

public interface LecturerTypeRepository extends JpaRepository<LecturerType, String> {
}
