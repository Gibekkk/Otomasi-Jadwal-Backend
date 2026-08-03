package com.moodbites.restfulapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moodbites.restfulapi.model.UserPreference;
import com.moodbites.restfulapi.model.enums.Mood;
import com.moodbites.restfulapi.model.User;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {
    public Optional<UserPreference> findByUserIdAndMood(User userId, Mood mood);
}
