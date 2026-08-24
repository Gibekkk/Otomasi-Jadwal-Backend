package com.jadwal.restfulapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.TimelineGeneration;

public interface TimelineGenerationRepository extends JpaRepository<TimelineGeneration, String> {
}