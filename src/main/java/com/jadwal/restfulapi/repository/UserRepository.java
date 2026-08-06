package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    public Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    public Optional<User> findByIdAndDeletedAtIsNull(String id);
    public List<User> findAllByDeletedAtIsNull();
}
