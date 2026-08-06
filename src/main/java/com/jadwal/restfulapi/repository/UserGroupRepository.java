package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, String> {
    public Optional<UserGroup> findByIdAndDeletedAtIsNull(String id);
    public List<UserGroup> findAllByDeletedAtIsNull();
    public Optional<UserGroup> findByNameAndDeletedAtIsNull(String name);
    public Optional<UserGroup> findByIdAndNameNotAndDeletedAtIsNull(String id, String name);
}
