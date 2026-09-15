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
 * Full-stack integration test: real embedded servlet container, real H2
 * database, and a plain {@link TestRestTemplate} client -- no mocks anywhere
 * in the request path. This walks through the whole
 * login -> create -> read -> update -> delete lifecycle for a category the
 * way a real frontend client would.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CategoryLifecycleIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasherMatcher passwordHasherMatcher;

    private String baseUrl;
    private String rawPassword;
    private String username;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";

        username = "sa" + UUID.randomUUID().toString().substring(0, 10);
        rawPassword = "Sup3rSecret!";

        User superAdmin = new User();
        superAdmin.setUsername(username);
        superAdmin.setPassword(passwordHasherMatcher.hashPassword(rawPassword));
        superAdmin.setName("Integration Test Super Admin");
        superAdmin.setRole(Role.SUPERADMIN);
        superAdmin.setCreatedAt(LocalDateTime.now());
        superAdmin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(superAdmin);
    }

    private String loginAndGetToken() {
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", username);
        loginBody.put("password", rawPassword);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl + "/auth/login", loginBody, Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) loginResponse.getBody().get("token");
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        return headers;
    }

    @Test
    @DisplayName("A super admin can log in and fully manage a category's lifecycle end-to-end")
    void fullCategoryLifecycleThroughRealHttp() {
        String token = loginAndGetToken();
        String categoryName = "Kategori Integrasi " + UUID.randomUUID().toString().substring(0, 8);

        // 1. Create
        Map<String, String> createBody = new HashMap<>();
        createBody.put("name", categoryName);
        HttpEntity<Map<String, String>> createRequest = new HttpEntity<>(createBody, authHeaders(token));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(baseUrl + "/category", createRequest,
                Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String categoryId = (String) createResponse.getBody().get("categoryId");
        assertThat(categoryId).isNotBlank();

        // 2. Read
        ResponseEntity<Map> getResponse = restTemplate.exchange(baseUrl + "/category/" + categoryId,
                HttpMethod.GET, new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("name")).isEqualTo(categoryName);

        // 3. Update
        String updatedName = categoryName + " (Updated)";
        Map<String, String> editBody = new HashMap<>();
        editBody.put("name", updatedName);
        ResponseEntity<Map> editResponse = restTemplate.exchange(baseUrl + "/category/" + categoryId,
                HttpMethod.PUT, new HttpEntity<>(editBody, authHeaders(token)), Map.class);
        assertThat(editResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(editResponse.getBody().get("name")).isEqualTo(updatedName);

        // 4. Delete
        ResponseEntity<Map> deleteResponse = restTemplate.exchange(baseUrl + "/category/" + categoryId,
                HttpMethod.DELETE, new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 5. Verify it is really gone
        ResponseEntity<Map> verifyGoneResponse = restTemplate.exchange(baseUrl + "/category/" + categoryId,
                HttpMethod.GET, new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(verifyGoneResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Category endpoints reject requests that carry no session token at all")
    void categoryEndpointsRejectMissingToken() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/category", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
