package com.jadwal.restfulapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.UserPreference;
import com.jadwal.restfulapi.model.enums.Mood;
import com.jadwal.restfulapi.model.User;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {
    public Optional<UserPreference> findByUserIdAndMood(User userId, Mood mood);
}
