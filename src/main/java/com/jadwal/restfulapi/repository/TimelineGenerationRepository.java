package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.TimelineGeneration;

public interface TimelineGenerationRepository extends JpaRepository<TimelineGeneration, String> {
}