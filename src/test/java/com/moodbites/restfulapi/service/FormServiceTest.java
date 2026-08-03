package com.moodbites.restfulapi.service;

import com.moodbites.restfulapi.dto.MoodFormDTO;
import com.moodbites.restfulapi.dto.constructs.FlavorProfile;
import com.moodbites.restfulapi.dto.constructs.MoodProfile;
import com.moodbites.restfulapi.model.User;
import com.moodbites.restfulapi.model.UserFlavorPreference;
import com.moodbites.restfulapi.model.UserPreference;
import com.moodbites.restfulapi.model.UserSampleFoodPreference;
import com.moodbites.restfulapi.model.enums.Flavor;
import com.moodbites.restfulapi.model.enums.Mood;
import com.moodbites.restfulapi.model.enums.SampleFood;
import com.moodbites.restfulapi.repository.UserFlavorPreferenceRepository;
import com.moodbites.restfulapi.repository.UserPreferenceRepository;
import com.moodbites.restfulapi.repository.UserRepository;
import com.moodbites.restfulapi.repository.UserSampleFoodPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormService Unit Tests")
class FormServiceTest {

    @Mock
    private UserSampleFoodPreferenceRepository userSampleFoodPreferenceRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private UserFlavorPreferenceRepository userFlavorPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FormService formService;

    private User mockUser;
    private UserPreference mockUserPreference;
    private UserFlavorPreference mockFlavorPreferenceSweet;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-uuid-123");

        mockUserPreference = new UserPreference();
        mockUserPreference.setId("pref-uuid-123"); // ID sekarang String
        mockUserPreference.setUserId(mockUser);
        mockUserPreference.setMood(Mood.HAPPY); 

        mockFlavorPreferenceSweet = new UserFlavorPreference();
        mockFlavorPreferenceSweet.setId("flavor-uuid-123"); // ID sekarang String
        mockFlavorPreferenceSweet.setUserPreferenceId(mockUserPreference);
        mockFlavorPreferenceSweet.setFlavor(Flavor.SWEET);

        // Relasi menggunakan Set sesuai Entity
        mockUser.setUserPreferences(Set.of(mockUserPreference));
        mockUserPreference.setUserFlavorPreferences(Set.of(mockFlavorPreferenceSweet));
        mockUserPreference.setUserSampleFoodPreferences(new HashSet<>());
    }

    // =====================================================================
    // createUserPreferences() & createUserMoodPreferences()
    // =====================================================================

    @Nested
    @DisplayName("createUserPreferences() & Sub-methods")
    class CreateUserPreferences {

        @Test
        @DisplayName("Iterates over Mood enum and saves new UserPreferences")
        void whenCreateUserMoodPreferences_thenSavesAllMoods() {
            formService.createUserMoodPreferences(mockUser);

            int expectedMoodCount = Mood.getMoodList().size();
            verify(userPreferenceRepository, times(expectedMoodCount)).save(any(UserPreference.class));
        }

        @Test
        @DisplayName("Iterates over user's preferences and Flavor enum to save UserFlavorPreferences")
        void whenCreateUserFlavorPreferences_thenSavesAllFlavors() {
            formService.createUserFlavorPreferences(mockUser);

            int expectedFlavorCount = mockUser.getUserPreferences().size() * Flavor.getFlavorList().size();
            verify(userFlavorPreferenceRepository, times(expectedFlavorCount)).save(any(UserFlavorPreference.class));
        }

        @Test
        @DisplayName("createUserPreferences calls both mood and flavor creation logic")
        void whenCreateUserPreferences_thenBothRepositoriesAreSaved() {
            formService.createUserPreferences(mockUser);

            verify(userPreferenceRepository, atLeastOnce()).save(any(UserPreference.class));
            verify(userFlavorPreferenceRepository, atLeastOnce()).save(any(UserFlavorPreference.class));
        }
    }

    // =====================================================================
    // updateUserPreferences()
    // =====================================================================

    @Nested
    @DisplayName("updateUserPreferences()")
    class UpdateUserPreferences {

        @Test
        @DisplayName("Updates flavor scales, handles sample foods, and saves user")
        void whenValidDtoProvided_thenUpdatesPreferencesAndSavesUser() {
            // 1. Arrange & Mock DTO Structure
            MoodFormDTO mockFormDto = mock(MoodFormDTO.class);
            MoodProfile mockMoodProfile = mock(MoodProfile.class);
            FlavorProfile mockDesires = mock(FlavorProfile.class);
            FlavorProfile mockIntensities = mock(FlavorProfile.class);

            // Mock scales for SWEET (Mapping ke getManis() dari FlavorProfile)
            when(mockDesires.getManis()).thenReturn(4);
            when(mockIntensities.getManis()).thenReturn(5);

            when(mockMoodProfile.getDesire()).thenReturn(mockDesires);
            when(mockMoodProfile.getIntensity()).thenReturn(mockIntensities);
            
            // Menggunakan nilai Enum asli berdasarkan method fromString()
            when(mockMoodProfile.getCategories()).thenReturn(List.of("Nasi Goreng Merah"));

            // Map struktur DTO (Key harus cocok dengan Mood.HAPPY.toString().toLowerCase() -> "happy")
            Map<String, MoodProfile> moodMap = new HashMap<>();
            moodMap.put(Mood.HAPPY.toString().toLowerCase(), mockMoodProfile);
            when(mockFormDto.getMoods()).thenReturn(moodMap);

            when(userSampleFoodPreferenceRepository.findByUserPreferenceIdAndSampleFood(any(), any()))
                    .thenReturn(Optional.empty());

            // 2. Act
            formService.updateUserPreferences(mockUser, mockFormDto);

            // 3. Assert Flavor Preference ter-update
            assertThat(mockFlavorPreferenceSweet.getPreferenceScale()).isEqualTo(4);
            assertThat(mockFlavorPreferenceSweet.getIntensityScale()).isEqualTo(5);
            assertThat(mockFlavorPreferenceSweet.getEditedAt()).isNotNull();
            verify(userFlavorPreferenceRepository).save(mockFlavorPreferenceSweet);

            // 4. Assert Sample Food dibuat baru
            ArgumentCaptor<UserSampleFoodPreference> sampleCaptor = ArgumentCaptor.forClass(UserSampleFoodPreference.class);
            verify(userSampleFoodPreferenceRepository).save(sampleCaptor.capture());
            
            // Memastikan data yang tersimpan sama dengan enum SampleFood.NASI_GORENG_MERAH
            assertThat(sampleCaptor.getValue().getSampleFood()).isEqualTo(SampleFood.NASI_GORENG_MERAH);

            // 5. Assert User di-update
            assertThat(mockUser.getEditedPreferenceAt()).isNotNull();
            verify(userRepository).save(mockUser);
        }
    }

    // =====================================================================
    // clearUserSampleFoodPreferences()
    // =====================================================================

    @Nested
    @DisplayName("clearUserSampleFoodPreferences()")
    class ClearUserSampleFoodPreferences {

        @Test
        @DisplayName("Deletes all existing sample food preferences for a given user preference")
        void whenHasSampleFoods_thenDeletesThemAll() {
            // Arrange
            UserSampleFoodPreference sample1 = new UserSampleFoodPreference();
            UserSampleFoodPreference sample2 = new UserSampleFoodPreference();
            
            // Relasi ini menggunakan Set
            mockUserPreference.setUserSampleFoodPreferences(Set.of(sample1, sample2));

            // Act
            formService.clearUserSampleFoodPreferences(mockUserPreference);

            // Assert
            verify(userSampleFoodPreferenceRepository).delete(sample1);
            verify(userSampleFoodPreferenceRepository).delete(sample2);
            verify(userSampleFoodPreferenceRepository, times(2)).delete(any(UserSampleFoodPreference.class));
        }
    }

    // =====================================================================
    // getPreferenceByMoodAndUser()
    // =====================================================================

    @Nested
    @DisplayName("getPreferenceByMoodAndUser()")
    class GetPreferenceByMoodAndUser {

        @Test
        @DisplayName("Returns structured map data when user preference is found")
        void whenPreferenceExists_thenReturnsFormattedMap() {
            // Arrange
            when(userPreferenceRepository.findByUserIdAndMood(mockUser, Mood.HAPPY))
                    .thenReturn(Optional.of(mockUserPreference));

            // Setup scales
            mockFlavorPreferenceSweet.setPreferenceScale(3);
            mockFlavorPreferenceSweet.setIntensityScale(4);

            // Setup dummy categories
            UserSampleFoodPreference mockSampleFood = new UserSampleFoodPreference();
            mockSampleFood.setSampleFood(SampleFood.BEEF_TERIYAKI); // Valid Enum
            
            // Set (bukan List)
            mockUserPreference.setUserSampleFoodPreferences(Set.of(mockSampleFood));

            // Act
            Map<String, Object> response = formService.getPreferenceByMoodAndUser(Mood.HAPPY, mockUser);

            // Assert
            assertThat(response).containsKey("preferences");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> preferences = (Map<String, Object>) response.get("preferences");
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> desire = (Map<String, Integer>) preferences.get("desire");
            // Harus cocok dengan nilai dari Flavor.SWEET.toString() yaitu "Manis"
            assertThat(desire.get(Flavor.SWEET.toString())).isEqualTo(3);

            @SuppressWarnings("unchecked")
            Map<String, Integer> intensity = (Map<String, Integer>) preferences.get("intensity");
            assertThat(intensity.get(Flavor.SWEET.toString())).isEqualTo(4);

            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) preferences.get("categories");
            // Harus cocok dengan string asli "Beef Teriyaki"
            assertThat(categories).containsExactly(SampleFood.BEEF_TERIYAKI.toString());
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when user preference is not found")
        void whenPreferenceNotFound_thenThrowsException() {
            // Arrange
            when(userPreferenceRepository.findByUserIdAndMood(mockUser, Mood.SAD))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> formService.getPreferenceByMoodAndUser(Mood.SAD, mockUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User preference not found");
        }
    }
}