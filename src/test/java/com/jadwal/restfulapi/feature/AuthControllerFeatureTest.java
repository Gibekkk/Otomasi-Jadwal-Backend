package com.jadwal.restfulapi.feature;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Role;
import com.jadwal.restfulapi.repository.UserRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

/**
 * Feature tests for {@code /api/v1/auth/*}. These boot the full Spring
 * context against the in-memory H2 database configured in
 * src/test/resources/application.properties, so they exercise the real
 * AuthService + repositories behind MockMvc instead of mocking the service
 * layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerFeatureTest {

    private static final String BASE_URL = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasherMatcher passwordHasherMatcher;

    @Autowired
    private ObjectMapper objectMapper;

    private User superAdmin;

    @BeforeEach
    void setUp() {
        superAdmin = new User();
        superAdmin.setUsername("featuretest");
        superAdmin.setPassword(passwordHasherMatcher.hashPassword("correct-password"));
        superAdmin.setName("Feature Test Admin");
        superAdmin.setRole(Role.SUPERADMIN);
        superAdmin.setCreatedAt(LocalDateTime.now());
        superAdmin.setUpdatedAt(LocalDateTime.now());
        superAdmin = userRepository.save(superAdmin);
    }

    @Test
    @DisplayName("POST /auth/login returns 200 with a session token for correct credentials")
    void loginWithValidCredentialsReturnsToken() throws Exception {
        String body = "{\"username\":\"featuretest\",\"password\":\"correct-password\"}";

        mockMvc.perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(superAdmin.getId()))
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /auth/login returns 403 for a wrong password")
    void loginWithWrongPasswordReturnsForbidden() throws Exception {
        String body = "{\"username\":\"featuretest\",\"password\":\"wrong-password\"}";

        mockMvc.perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Username or Password is Incorrect"));
    }

    @Test
    @DisplayName("POST /auth/login returns 400 when the username is missing")
    void loginWithMissingUsernameReturnsBadRequest() throws Exception {
        String body = "{\"password\":\"correct-password\"}";

        mockMvc.perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username Cannot Be NULL"));
    }

    @Test
    @DisplayName("GET /auth/check returns 401 when no Token header is sent")
    void checkTokenWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/check"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication Failed"));
    }

    @Test
    @DisplayName("Login then GET /auth/check with the returned token identifies the user")
    void checkTokenWithValidTokenReturnsUserInfo() throws Exception {
        String loginBody = "{\"username\":\"featuretest\",\"password\":\"correct-password\"}";
        String loginResponse = mockMvc
                .perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(get(BASE_URL + "/check").header("Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("featuretest"))
                .andExpect(jsonPath("$.role").value(Role.SUPERADMIN.toString()));
    }

    @Test
    @DisplayName("Login then logout invalidates the token for subsequent requests")
    void logoutInvalidatesTheSessionToken() throws Exception {
        String loginBody = "{\"username\":\"featuretest\",\"password\":\"correct-password\"}";
        String loginResponse = mockMvc
                .perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(post(BASE_URL + "/logout").header("Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout Successful"));

        mockMvc.perform(get(BASE_URL + "/check").header("Token", token))
                .andExpect(status().isUnauthorized());
    }
}
