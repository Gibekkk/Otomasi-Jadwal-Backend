package com.jadwal.restfulapi.feature;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Role;
import com.jadwal.restfulapi.repository.CategoryRepository;
import com.jadwal.restfulapi.repository.SessionRepository;
import com.jadwal.restfulapi.repository.UserRepository;

/**
 * Feature tests for {@code /api/v1/category}, focused on the role-based
 * access rules implemented directly in {@code CategoryController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerFeatureTest {

    private static final String BASE_URL = "/api/v1/category";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User superAdmin;
    private User prodiAdmin;
    private String superAdminToken;
    private String prodiAdminToken;
    private Category wajibCategory;

    @BeforeEach
    void setUp() {
        superAdmin = persistUser(Role.SUPERADMIN, "superadmin-cat", null);
        superAdminToken = persistSession(superAdmin).getId();

        Category prodi = new Category();
        prodi.setName("Informatika");
        prodi.setIsProdi(true);
        prodi.setCreatedBy(superAdmin);
        prodi.setEditedBy(superAdmin);
        prodi.setCreatedAt(LocalDateTime.now());
        prodi.setUpdatedAt(LocalDateTime.now());
        prodi = categoryRepository.save(prodi);

        prodiAdmin = persistUser(Role.PRODI, "prodiadmin-cat", prodi);
        prodiAdminToken = persistSession(prodiAdmin).getId();

        wajibCategory = new Category();
        wajibCategory.setName("Wajib");
        wajibCategory.setIsProdi(false);
        wajibCategory.setCreatedBy(superAdmin);
        wajibCategory.setEditedBy(superAdmin);
        wajibCategory.setCreatedAt(LocalDateTime.now());
        wajibCategory.setUpdatedAt(LocalDateTime.now());
        wajibCategory = categoryRepository.save(wajibCategory);
    }

    private User persistUser(Role role, String username, Category prodi) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("irrelevant-for-these-tests");
        user.setName("Test " + username);
        user.setRole(role);
        user.setProdiId(prodi);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Session persistSession(User user) {
        Session session = new Session();
        session.setUserId(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastSeenAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Test
    @DisplayName("GET /category/all is public and lists categories without requiring a token")
    void getAllCategoryIsPublic() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Wajib')]").exists());
    }

    @Test
    @DisplayName("GET /category without a token is rejected with 401")
    void getCategoryWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication Failed"));
    }

    @Test
    @DisplayName("GET /category as super admin succeeds and returns the seeded category")
    void getCategoryAsSuperAdminSucceeds() throws Exception {
        mockMvc.perform(get(BASE_URL).header("Token", superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + wajibCategory.getId() + "')]").exists());
    }

    @Test
    @DisplayName("POST /category as a prodi admin is forbidden (only super admin / BAA may create)")
    void createCategoryAsProdiAdminIsForbidden() throws Exception {
        String body = "{\"name\":\"Kategori Baru\"}";

        mockMvc.perform(post(BASE_URL).header("Token", prodiAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @DisplayName("POST /category as super admin creates a new category")
    void createCategoryAsSuperAdminSucceeds() throws Exception {
        String body = "{\"name\":\"Kategori Baru\"}";

        mockMvc.perform(post(BASE_URL).header("Token", superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kategori Baru"))
                .andExpect(jsonPath("$.isProdi").value(false));
    }

    @Test
    @DisplayName("POST /category with a duplicate name returns 400")
    void createCategoryWithDuplicateNameReturnsBadRequest() throws Exception {
        String body = "{\"name\":\"Wajib\"}";

        mockMvc.perform(post(BASE_URL).header("Token", superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name Already Exist"));
    }

    @Test
    @DisplayName("GET /category/{id} for an unknown id returns 404")
    void getCategoryByIdNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/does-not-exist").header("Token", superAdminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category Not Found"));
    }

    @Test
    @DisplayName("DELETE /category/{id} as super admin soft deletes the category")
    void deleteCategoryAsSuperAdminSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + wajibCategory.getId()).header("Token", superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category Deleted Successfully"));

        mockMvc.perform(get(BASE_URL + "/" + wajibCategory.getId()).header("Token", superAdminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /category/{id} as a prodi admin is forbidden")
    void deleteCategoryAsProdiAdminIsForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + wajibCategory.getId()).header("Token", prodiAdminToken))
                .andExpect(status().isForbidden());
    }
}
