package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, String> {
    public Optional<UserGroup> findById(String id);
    public List<UserGroup> findAll();
}
