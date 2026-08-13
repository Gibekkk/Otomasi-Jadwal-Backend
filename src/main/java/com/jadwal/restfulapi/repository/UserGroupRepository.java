package com.jadwal.restfulapi.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, String> {
    public Optional<UserGroup> findByName(String name);
    public Optional<UserGroup> findByIdAndNameNot(String id, String name);
    public List<UserGroup> findByNameNot(String name);
}
