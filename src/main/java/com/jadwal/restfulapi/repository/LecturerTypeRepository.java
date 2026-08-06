package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.LecturerType;

public interface LecturerTypeRepository extends JpaRepository<LecturerType, String> {
}
