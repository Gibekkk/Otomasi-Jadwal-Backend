package com.jadwal.restfulapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jadwal.restfulapi.dto.MoodFormDTO;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.service.FormService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FormController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass filter security untuk fokus ke logic controller
@TestPropertySource(properties = "storage.api-prefix=/api") // Menetapkan path API
@DisplayName("FormController Web Layer Tests")
class FormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private FormService formService;

    // Tambahan MockBean untuk mencegah crash akibat dependensi Firebase
    @MockBean
    private FirebaseMessaging firebaseMessaging;

    private final String BASE_URL = "/api/form";
    private User mockUser;
    private Session mockSession;
    private String validJsonPayload;

    @BeforeEach
    void setUp() throws Exception {
        mockUser = new User();
        mockUser.setId("user-uuid-123");

        mockSession = new Session();
        mockSession.setToken("valid-session-token");
        mockSession.setUserId(mockUser);

        // Membangun payload JSON yang valid agar lolos validasi moodFormDTO.checkDTO()
        Map<String, Object> flavorProfile = Map.of(
                "Manis", 3,
                "Pedas", 3,
                "Asin / Gurih", 3,
                "Asam / Segar", 3,
                "Pahit", 3
        );

        Map<String, Object> moodProfile = Map.of(
                "desire", flavorProfile,
                "intensity", flavorProfile,
                "categories", List.of("Nasi Goreng Merah")
        );

        // Memasukkan 4 mood wajib sesuai enum Mood
        Map<String, Object> moods = Map.of(
                "sad", moodProfile,
                "angry", moodProfile,
                "happy", moodProfile,
                "neutral", moodProfile
        );

        Map<String, Object> payload = Map.of("moods", moods);
        validJsonPayload = objectMapper.writeValueAsString(payload);
    }

    // =====================================================================
    // POST /form
    // =====================================================================
    @Nested
    @DisplayName("POST /form")
    class EditPreferences {

        @Test
        @DisplayName("Returns 200 OK and updates preferences when token and DTO are valid")
        void whenValidRequest_thenReturns200() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenReturn(Optional.of(mockSession));

            mockMvc.perform(post(BASE_URL)
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJsonPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("user-uuid-123"))
                    .andExpect(jsonPath("$.message").value("Preferences updated successfully"));

            verify(formService).updateUserPreferences(eq(mockUser), any(MoodFormDTO.class));
        }

        @Test
        @DisplayName("Returns 403 FORBIDDEN when session token is invalid or missing")
        void whenInvalidToken_thenReturns403() throws Exception {
            when(authService.findSessionBySessionToken("invalid-token"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post(BASE_URL)
                            .header("Token", "invalid-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJsonPayload))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Authentication Failed"));

            verify(formService, never()).updateUserPreferences(any(), any());
        }

        @Test
        @DisplayName("Returns 400 BAD REQUEST when DTO validation fails")
        void whenInvalidDTO_thenReturns400() throws Exception {
            // Mengirim JSON kosong "{}" akan memicu moodFormDTO.checkDTO() melempar IllegalArgumentException
            String invalidJsonPayload = "{}";

            mockMvc.perform(post(BASE_URL)
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJsonPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Moods Cannot Be NULL or Empty"));

            verify(authService, never()).findSessionBySessionToken(any());
            verify(formService, never()).updateUserPreferences(any(), any());
        }

        @Test
        @DisplayName("Returns 500 INTERNAL SERVER ERROR when an unexpected exception occurs")
        void whenUnexpectedException_thenReturns500() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenThrow(new RuntimeException("Database error simulation"));

            mockMvc.perform(post(BASE_URL)
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJsonPayload))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Database error simulation"));
        }
    }
}