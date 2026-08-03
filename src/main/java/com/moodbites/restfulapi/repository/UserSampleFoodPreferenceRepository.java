package com.moodbites.restfulapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moodbites.restfulapi.model.UserPreference;
import com.moodbites.restfulapi.model.UserSampleFoodPreference;
import com.moodbites.restfulapi.model.enums.SampleFood;

public interface UserSampleFoodPreferenceRepository extends JpaRepository<UserSampleFoodPreference, String> {
    public Optional<UserSampleFoodPreference> findByUserPreferenceIdAndSampleFood(UserPreference userPreference, SampleFood sampleFood);
}
