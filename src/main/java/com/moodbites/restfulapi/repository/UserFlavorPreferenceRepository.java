package com.moodbites.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moodbites.restfulapi.model.UserFlavorPreference;

public interface UserFlavorPreferenceRepository extends JpaRepository<UserFlavorPreference, String> {
}
