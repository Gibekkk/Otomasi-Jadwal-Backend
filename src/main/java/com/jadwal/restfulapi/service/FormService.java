package com.jadwal.restfulapi.service;

import com.jadwal.restfulapi.repository.UserSampleFoodPreferenceRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.dto.MoodFormDTO;
import com.jadwal.restfulapi.dto.constructs.FlavorProfile;
import com.jadwal.restfulapi.dto.constructs.MoodProfile;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.UserFlavorPreference;
import com.jadwal.restfulapi.model.UserPreference;
import com.jadwal.restfulapi.model.UserSampleFoodPreference;
import com.jadwal.restfulapi.model.enums.Flavor;
import com.jadwal.restfulapi.model.enums.Mood;
import com.jadwal.restfulapi.model.enums.SampleFood;
import com.jadwal.restfulapi.repository.UserPreferenceRepository;
import com.jadwal.restfulapi.repository.UserRepository;
import com.jadwal.restfulapi.repository.UserFlavorPreferenceRepository;

@Service
public class FormService {

    @Autowired
    private UserSampleFoodPreferenceRepository userSampleFoodPreferenceRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserFlavorPreferenceRepository userFlavorPreferenceRepository;

    @Autowired
    private UserRepository userRepository;

    public void createUserPreferences(User user) {
        createUserMoodPreferences(user);
        createUserFlavorPreferences(user);
    }

    public void createUserMoodPreferences(User user) {
        for (Mood mood : Mood.getMoodList()) {
            UserPreference userPreference = new UserPreference();
            userPreference.setUserId(user);
            userPreference.setMood(mood);
            userPreferenceRepository.save(userPreference);
        }
    }

    public void createUserFlavorPreferences(User user) {
        for (UserPreference userPreference : user.getUserPreferences()) {
            for (Flavor flavor : Flavor.getFlavorList()) {
                UserFlavorPreference userFlavorPreference = new UserFlavorPreference();
                userFlavorPreference.setUserPreferenceId(userPreference);
                userFlavorPreference.setFlavor(flavor);
                userFlavorPreference.setEditedAt(LocalDateTime.now());
                userFlavorPreferenceRepository.save(userFlavorPreference);
            }
        }
    }

    public void updateUserPreferences(User user, MoodFormDTO moodFormDTO) {
        Map<String, MoodProfile> moodProfiles = moodFormDTO.getMoods();
        for (UserPreference userPreference : user.getUserPreferences()) {
            MoodProfile moodProfile = moodProfiles.get(userPreference.getMood().toString().toLowerCase());
            FlavorProfile desires = moodProfile.getDesire();
            FlavorProfile intensities = moodProfile.getIntensity();
            List<String> categories = moodProfile.getCategories();
            for (UserFlavorPreference userFlavorPreference : userPreference.getUserFlavorPreferences()) {
                switch (userFlavorPreference.getFlavor()) {
                    case SWEET -> {
                        userFlavorPreference.setPreferenceScale(desires.getManis());
                        userFlavorPreference.setIntensityScale(intensities.getManis());
                    }
                    case SOUR -> {
                        userFlavorPreference.setPreferenceScale(desires.getAsamSegar());
                        userFlavorPreference.setIntensityScale(intensities.getAsamSegar());
                    }
                    case SALTY -> {
                        userFlavorPreference.setPreferenceScale(desires.getAsinGurih());
                        userFlavorPreference.setIntensityScale(intensities.getAsinGurih());
                    }
                    case BITTER -> {
                        userFlavorPreference.setPreferenceScale(desires.getPahit());
                        userFlavorPreference.setIntensityScale(intensities.getPahit());
                    }
                    case SPICY -> {
                        userFlavorPreference.setPreferenceScale(desires.getPedas());
                        userFlavorPreference.setIntensityScale(intensities.getPedas());
                    }
                }
                userFlavorPreference.setEditedAt(LocalDateTime.now());
                userFlavorPreferenceRepository.save(userFlavorPreference);
            }

            clearUserSampleFoodPreferences(userPreference);
            if (categories != null && !categories.isEmpty()) {
                for (String category : categories) {
                    if (userSampleFoodPreferenceRepository.findByUserPreferenceIdAndSampleFood(userPreference, SampleFood.fromString(category))
                            .isPresent())
                        continue;
                    UserSampleFoodPreference userSampleFoodPreference = new UserSampleFoodPreference();
                    userSampleFoodPreference.setUserPreferenceId(userPreference);
                    userSampleFoodPreference.setSampleFood(SampleFood.fromString(category));
                    userSampleFoodPreference.setCreatedAt(LocalDateTime.now());
                    userSampleFoodPreferenceRepository.save(userSampleFoodPreference);
                }
            }
        }
        user.setEditedPreferenceAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void clearUserSampleFoodPreferences(UserPreference userPreference) {
        for (UserSampleFoodPreference userSampleFoodPreference : userPreference.getUserSampleFoodPreferences()) {
            userSampleFoodPreferenceRepository.delete(userSampleFoodPreference);
        }
    }

    public Map<String, Object> getPreferenceByMoodAndUser(Mood mood, User user) {
        Optional<UserPreference> userPreferenceOpt = userPreferenceRepository.findByUserIdAndMood(user, mood);
        if(userPreferenceOpt.isEmpty()) {
            throw new IllegalArgumentException("User preference not found");
        }

        UserPreference userPreference = userPreferenceOpt.get();

        Map<String, Integer> desire = new HashMap<>();
        Map<String, Integer> intensity = new HashMap<>();
        for(UserFlavorPreference userFlavorPreference : userPreference.getUserFlavorPreferences()) {
            desire.put(userFlavorPreference.getFlavor().toString(), userFlavorPreference.getPreferenceScale());
            intensity.put(userFlavorPreference.getFlavor().toString(), userFlavorPreference.getIntensityScale());
        }

        ArrayList<String> categories = new ArrayList<>();
        for(UserSampleFoodPreference userSampleFoodPreference : userPreference.getUserSampleFoodPreferences()) {
            categories.add(userSampleFoodPreference.getSampleFood().toString());
        }

        Map<String, Object> response = Map.of(
            "preferences", Map.of(
                "desire", desire,
                "intensity", intensity,
                "categories", categories
            )
        );
        return response;
    }
}
