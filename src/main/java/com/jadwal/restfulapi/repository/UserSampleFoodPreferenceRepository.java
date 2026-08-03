package com.jadwal.restfulapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.UserPreference;
import com.jadwal.restfulapi.model.UserSampleFoodPreference;
import com.jadwal.restfulapi.model.enums.SampleFood;

public interface UserSampleFoodPreferenceRepository extends JpaRepository<UserSampleFoodPreference, String> {
    public Optional<UserSampleFoodPreference> findByUserPreferenceIdAndSampleFood(UserPreference userPreference, SampleFood sampleFood);
}
