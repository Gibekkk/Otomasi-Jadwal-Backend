package com.moodbites.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moodbites.restfulapi.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    public Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
