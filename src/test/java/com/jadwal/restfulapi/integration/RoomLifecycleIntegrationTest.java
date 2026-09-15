package com.jadwal.restfulapi.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Role;
import com.jadwal.restfulapi.repository.UserRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

/**
 * Full-stack integration test covering the room lifecycle: real embedded
 * server, real H2 database, plain HTTP client. Also verifies that the
 * role restriction (super admin / PM admin only) is enforced end-to-end.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RoomLifecycleIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasherMatcher passwordHasherMatcher;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
    }

    private String loginAndGetToken(String username, String rawPassword) {
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", username);
        loginBody.put("password", rawPassword);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl + "/auth/login", loginBody, Map.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) loginResponse.getBody().get("token");
    }

    private String createUserAndLogin(Role role, String usernamePrefix) {
        String username = usernamePrefix + UUID.randomUUID().toString().substring(0, 10 - usernamePrefix.length());
        String rawPassword = "Sup3rSecret!";

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordHasherMatcher.hashPassword(rawPassword));
        user.setName("Integration Test " + role);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return loginAndGetToken(username, rawPassword);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        return headers;
    }

    @Test
    @DisplayName("A PM admin can log in and fully manage a room's lifecycle end-to-end")
    void fullRoomLifecycleThroughRealHttp() {
        String token = createUserAndLogin(Role.PM, "pm");
        String roomName = "Ruang Integrasi " + UUID.randomUUID().toString().substring(0, 8);

        // 1. Create
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("name", roomName);
        createBody.put("capacity", 42);
        HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(createBody, authHeaders(token));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(baseUrl + "/room", createRequest, Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String roomId = (String) createResponse.getBody().get("id");
        assertThat(roomId).isNotBlank();
        assertThat(createResponse.getBody().get("capacity")).isEqualTo(42);

        // 2. Read
        ResponseEntity<Map> getResponse = restTemplate.exchange(baseUrl + "/room/" + roomId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("name")).isEqualTo(roomName);

        // 3. Update
        Map<String, Object> editBody = new HashMap<>();
        editBody.put("name", roomName);
        editBody.put("capacity", 60);
        ResponseEntity<Map> editResponse = restTemplate.exchange(baseUrl + "/room/" + roomId, HttpMethod.PUT,
                new HttpEntity<>(editBody, authHeaders(token)), Map.class);
        assertThat(editResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(editResponse.getBody().get("capacity")).isEqualTo(60);

        // 4. Delete
        ResponseEntity<Map> deleteResponse = restTemplate.exchange(baseUrl + "/room/" + roomId, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 5. Verify it is really gone
        ResponseEntity<Map> verifyGoneResponse = restTemplate.exchange(baseUrl + "/room/" + roomId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(verifyGoneResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("A BAA admin cannot create a room even with a valid, authenticated session")
    void baaAdminCannotCreateRoomThroughRealHttp() {
        String token = createUserAndLogin(Role.BAA, "baa");

        Map<String, Object> createBody = new HashMap<>();
        createBody.put("name", "Should Not Be Created");
        createBody.put("capacity", 20);
        HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(createBody, authHeaders(token));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(baseUrl + "/room", createRequest, Map.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
