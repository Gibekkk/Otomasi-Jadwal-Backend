package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.UserFlavorPreference;

public interface UserFlavorPreferenceRepository extends JpaRepository<UserFlavorPreference, String> {
}
