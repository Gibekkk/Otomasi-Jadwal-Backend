package com.moodbites.restfulapi.controller;

import com.google.firebase.messaging.FirebaseMessaging;
import com.moodbites.restfulapi.model.User;
import com.moodbites.restfulapi.model.enums.Mood;
import com.moodbites.restfulapi.service.AuthService;
import com.moodbites.restfulapi.service.FormService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExternalController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass filter security
@TestPropertySource(properties = "storage.api-prefix=/api") // Mock path prefix
@DisplayName("ExternalController Web Layer Tests")
class ExternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FormService formService;

    // Tambahan MockBean untuk mencegah crash akibat dependensi Firebase
    @MockBean
    private FirebaseMessaging firebaseMessaging;

    private final String BASE_URL = "/api/external";
    private User mockUser;
    private Map<String, Object> mockPreferencesMap;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-123");

        // Simulasi response data dari FormService
        mockPreferencesMap = Map.of(
                "preferences", Map.of(
                        "desire", Map.of("Manis", 4),
                        "intensity", Map.of("Manis", 5),
                        "categories", new String[]{"Nasi Goreng Merah"}
                )
        );
    }

    @Nested
    @DisplayName("GET /{mood}/{userId}")
    class GetPreferences {

        @Test
        @DisplayName("Returns 200 OK with preferences data when Mood and User are valid")
        void whenValidMoodAndUser_thenReturns200() throws Exception {
            // "Happy" adalah string valid sesuai Mood.checkExist()
            when(authService.findUserById("user-123")).thenReturn(Optional.of(mockUser));
            when(formService.getPreferenceByMoodAndUser(eq(Mood.HAPPY), eq(mockUser)))
                    .thenReturn(mockPreferencesMap);

            mockMvc.perform(get(BASE_URL + "/Happy/user-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.preferences.desire.Manis").value(4))
                    .andExpect(jsonPath("$.preferences.intensity.Manis").value(5))
                    .andExpect(jsonPath("$.preferences.categories[0]").value("Nasi Goreng Merah"));
        }

        @Test
        @DisplayName("Returns 404 NOT FOUND when Mood string does not exist in Enum")
        void whenInvalidMood_thenReturns404() throws Exception {
            // "Galau" tidak ada di Enum Mood
            mockMvc.perform(get(BASE_URL + "/Galau/user-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Mood not found"));
        }

        @Test
        @DisplayName("Returns 404 NOT FOUND when User ID is not found in database")
        void whenUserNotFound_thenReturns404() throws Exception {
            when(authService.findUserById("ghost-id")).thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/Happy/ghost-id")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        @DisplayName("Returns 400 BAD REQUEST when IllegalArgumentException is thrown")
        void whenIllegalArgumentException_thenReturns400() throws Exception {
            when(authService.findUserById("user-123")).thenReturn(Optional.of(mockUser));
            
            // Mensimulasikan formService melempar error validasi
            when(formService.getPreferenceByMoodAndUser(any(Mood.class), any(User.class)))
                    .thenThrow(new IllegalArgumentException("User preference not found"));

            mockMvc.perform(get(BASE_URL + "/Happy/user-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User preference not found"));
        }

        @Test
        @DisplayName("Returns 500 INTERNAL SERVER ERROR when an unexpected Exception occurs")
        void whenUnexpectedException_thenReturns500() throws Exception {
            when(authService.findUserById("user-123")).thenReturn(Optional.of(mockUser));
            
            // Mensimulasikan error server/database yang tidak terduga
            when(formService.getPreferenceByMoodAndUser(any(Mood.class), any(User.class)))
                    .thenThrow(new RuntimeException("Database connection timeout"));

            mockMvc.perform(get(BASE_URL + "/Happy/user-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Database connection timeout"));
        }
    }
}