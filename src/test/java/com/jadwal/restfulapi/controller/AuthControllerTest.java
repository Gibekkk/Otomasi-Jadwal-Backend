package com.jadwal.restfulapi.controller;

import com.google.firebase.messaging.FirebaseMessaging;
import com.jadwal.restfulapi.dto.ProfileDTO;
import com.jadwal.restfulapi.model.OTP;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.service.EmailService;
import com.jadwal.restfulapi.service.FormService;
import com.jadwal.restfulapi.service.OTPService;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters jika ada saat testing
@TestPropertySource(properties = "storage.api-prefix=/api") // Mocking nilai application.properties
@DisplayName("AuthController Web Layer Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FormService formService;

    @MockBean
    private OTPService otpService;

    @MockBean
    private EmailService emailService;

    // Tambahan MockBean untuk mencegah crash akibat dependensi Firebase
    @MockBean
    private FirebaseMessaging firebaseMessaging;

    private User mockUser;
    private Session mockSession;
    private OTP mockOtp;
    private final String BASE_URL = "/api/auth";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setEmail("gibekkk@jadwal.com");
        mockUser.setName("Gibekkk");
        mockUser.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        // editedPreferenceAt dibiarkan null secara default untuk test /check

        mockSession = new Session();
        mockSession.setToken("valid-token-123");
        mockSession.setUserId(mockUser);

        mockOtp = new OTP();
        mockOtp.setValidUntil(LocalDateTime.now().plusMinutes(5));
    }

    // =====================================================================
    // POST /login
    // =====================================================================
    @Nested
    @DisplayName("POST /login")
    class Login {

        @Test
        @DisplayName("Returns 200 OK with userId and token when credentials are valid")
        void whenValidLogin_thenReturns200() throws Exception {
            when(authService.authenticateUser(any(), any(), any()))
                    .thenReturn(Optional.of(mockSession));

            // Simulasi JSON payload (karena DTO akan di-deserialize oleh Jackson)
            String jsonPayload = "{\"email\":\"test@test.com\",\"password\":\"pass123\",\"fcmToken\":\"fcm123\"}";

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("user-123"))
                    .andExpect(jsonPath("$.token").value("valid-token-123"));
        }

        @Test
        @DisplayName("Returns 401 UNAUTHORIZED when credentials are wrong")
        void whenInvalidLogin_thenReturns401() throws Exception {
            when(authService.authenticateUser(any(), any(), any()))
                    .thenReturn(Optional.empty());

            String jsonPayload = "{\"email\":\"test@test.com\",\"password\":\"wrongpass\",\"fcmToken\":\"fcm123\"}";

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Email or Password is Incorrect"));
        }
        
        @Test
        @DisplayName("Returns 400 BAD REQUEST when DTO validation throws IllegalArgumentException")
        void whenDTOInvalid_thenReturns400() throws Exception {
            // Simulasi DTO checkDTO() melemparkan error melalui mock service (karena checkDTO tidak dimock)
            when(authService.authenticateUser(any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Email Invalid or Exceeded Max Length"));

            String jsonPayload = "{\"email\":\"invalid-email\",\"password\":\"pass123\"}";

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email Invalid or Exceeded Max Length"));
        }
    }

    // =====================================================================
    // POST /register
    // =====================================================================
    @Nested
    @DisplayName("POST /register")
    class Register {

        @Test
        @DisplayName("Returns 200 OK and generates OTP when email is available")
        void whenEmailAvailable_thenReturns200() throws Exception {
            when(authService.isEmailAvailable(anyString())).thenReturn(true);
            when(authService.registerUser(any(), any(), any(), any())).thenReturn(mockUser);
            when(otpService.generateOTP(any(User.class))).thenReturn(mockOtp);

            String jsonPayload = "{\"email\":\"new@jadwal.com\",\"password\":\"pass123\",\"name\":\"New User\",\"fcmToken\":\"fcm\"}";

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("user-123"))
                    .andExpect(jsonPath("$.otpValidUntil").exists());
            
            verify(emailService).sendOTPRegisToLogin(any(User.class), any(OTP.class));
        }

        @Test
        @DisplayName("Returns 409 CONFLICT when email is already registered")
        void whenEmailTaken_thenReturns409() throws Exception {
            when(authService.isEmailAvailable(anyString())).thenReturn(false);

            String jsonPayload = "{\"email\":\"taken@jadwal.com\",\"password\":\"pass123\",\"name\":\"User\",\"fcmToken\":\"fcm\"}";

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Email has been registered"));
        }
    }

    // =====================================================================
    // POST /register/verify
    // =====================================================================
    @Nested
    @DisplayName("POST /register/verify")
    class RegisterVerify {

        @Test
        @DisplayName("Returns 200 OK, verifies user, and creates preferences when OTP is valid")
        void whenOtpValid_thenReturns200() throws Exception {
            when(otpService.verifyOTP(anyString(), anyString())).thenReturn(Optional.of(mockUser));
            when(authService.verifyUser(any(User.class))).thenReturn(mockSession);

            String jsonPayload = "{\"loginId\":\"user-123\",\"code\":\"123456\"}";

            mockMvc.perform(post(BASE_URL + "/register/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("user-123"))
                    .andExpect(jsonPath("$.token").value("valid-token-123"));

            verify(formService).createUserPreferences(mockUser);
        }

        @Test
        @DisplayName("Returns 401 UNAUTHORIZED when OTP is invalid or expired")
        void whenOtpInvalid_thenReturns401() throws Exception {
            when(otpService.verifyOTP(anyString(), anyString())).thenReturn(Optional.empty());

            String jsonPayload = "{\"loginId\":\"user-123\",\"code\":\"000000\"}";

            mockMvc.perform(post(BASE_URL + "/register/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("OTP Not Valid or Has Expired"));
        }
    }

    // =====================================================================
    // POST /refreshOtp/{userId}
    // =====================================================================
    @Nested
    @DisplayName("POST /refreshOtp/{userId}")
    class RefreshOtp {

        @Test
        @DisplayName("Returns 200 OK and sends new OTP email when user and OTP are found")
        void whenUserAndOtpFound_thenReturns200() throws Exception {
            when(authService.findUserById("user-123")).thenReturn(Optional.of(mockUser));
            when(otpService.refreshOTP(mockUser)).thenReturn(Optional.of(mockOtp));

            mockMvc.perform(post(BASE_URL + "/refreshOtp/user-123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("user-123"));

            verify(emailService).sendOTPRegisToLogin(mockUser, mockOtp);
        }

        @Test
        @DisplayName("Returns 404 NOT FOUND when user is found but OTP generation fails")
        void whenOtpNotFound_thenReturns404() throws Exception {
            when(authService.findUserById("user-123")).thenReturn(Optional.of(mockUser));
            when(otpService.refreshOTP(mockUser)).thenReturn(Optional.empty());

            mockMvc.perform(post(BASE_URL + "/refreshOtp/user-123"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("OTP Not Found"));
        }

        @Test
        @DisplayName("Returns 401 UNAUTHORIZED when user is not found")
        void whenUserNotFound_thenReturns401() throws Exception {
            when(authService.findUserById("invalid-id")).thenReturn(Optional.empty());

            mockMvc.perform(post(BASE_URL + "/refreshOtp/invalid-id"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Login ID Not Found"));
        }
    }

    // =====================================================================
    // GET /profile
    // =====================================================================
    @Nested
    @DisplayName("GET /profile")
    class GetProfile {

        @Test
        @DisplayName("Returns 200 OK with profile data when token is valid")
        void whenTokenValid_thenReturns200() throws Exception {
            when(authService.findSessionBySessionToken("valid-token-123")).thenReturn(Optional.of(mockSession));

            mockMvc.perform(get(BASE_URL + "/profile")
                            .header("Token", "valid-token-123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("user-123"))
                    .andExpect(jsonPath("$.email").value("gibekkk@jadwal.com"))
                    .andExpect(jsonPath("$.joinedYear").value(2026))
                    .andExpect(jsonPath("$.name").value("Gibekkk"));
        }

        @Test
        @DisplayName("Returns 403 FORBIDDEN when token is invalid")
        void whenTokenInvalid_thenReturns403() throws Exception {
            when(authService.findSessionBySessionToken("invalid-token")).thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/profile")
                            .header("Token", "invalid-token"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Authentication Failed"));
        }
    }

    // =====================================================================
    // PATCH /profile
    // =====================================================================
    @Nested
    @DisplayName("PATCH /profile")
    class EditProfile {

        @Test
        @DisplayName("Returns 200 OK and updates profile when token is valid")
        void whenTokenValid_thenReturns200() throws Exception {
            when(authService.findSessionBySessionToken("valid-token-123")).thenReturn(Optional.of(mockSession));
            
            User updatedUser = new User();
            updatedUser.setId("user-123");
            updatedUser.setEmail("gibekkk@jadwal.com");
            updatedUser.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
            updatedUser.setName("New Name");
            
            when(authService.editUserProfile(any(User.class), any(ProfileDTO.class))).thenReturn(updatedUser);

            String jsonPayload = "{\"name\":\"New Name\"}";

            mockMvc.perform(patch(BASE_URL + "/profile")
                            .header("Token", "valid-token-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Name"));
        }
    }

    // =====================================================================
    // POST /logout
    // =====================================================================
    @Nested
    @DisplayName("POST /logout")
    class Logout {

        @Test
        @DisplayName("Returns 200 OK and deletes session when token is valid")
        void whenTokenValid_thenReturns200() throws Exception {
            when(authService.findSessionBySessionToken("valid-token-123")).thenReturn(Optional.of(mockSession));

            mockMvc.perform(post(BASE_URL + "/logout")
                            .header("Token", "valid-token-123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logout Successful"));

            verify(authService).deleteSession(mockSession);
        }
    }

    // =====================================================================
    // GET /check
    // =====================================================================
    @Nested
    @DisplayName("GET /check")
    class CheckToken {

        @Test
        @DisplayName("Returns 200 OK with false isFinishedForm when preference is not edited")
        void whenNoPreference_thenReturnsFalse() throws Exception {
            when(authService.findSessionBySessionToken("valid-token-123")).thenReturn(Optional.of(mockSession));
            mockUser.setEditedPreferenceAt(null); // Ensure null

            mockMvc.perform(get(BASE_URL + "/check")
                            .header("Token", "valid-token-123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isFinishedForm").value(false));
        }

        @Test
        @DisplayName("Returns 200 OK with true isFinishedForm when preference is edited")
        void whenPreferenceEdited_thenReturnsTrue() throws Exception {
            when(authService.findSessionBySessionToken("valid-token-123")).thenReturn(Optional.of(mockSession));
            mockUser.setEditedPreferenceAt(LocalDateTime.now()); // User already finished form

            mockMvc.perform(get(BASE_URL + "/check")
                            .header("Token", "valid-token-123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isFinishedForm").value(true));
        }
    }
}