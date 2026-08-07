package com.jadwal.restfulapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.FreeTable;

public interface FreeTableRepository extends JpaRepository<FreeTable, String> {
    Optional<FreeTable> findByYearAndIsOdd(int year, Boolean isOdd);
}