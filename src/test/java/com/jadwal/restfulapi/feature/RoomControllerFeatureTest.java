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

import com.jadwal.restfulapi.model.Room;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Role;
import com.jadwal.restfulapi.repository.RoomRepository;
import com.jadwal.restfulapi.repository.SessionRepository;
import com.jadwal.restfulapi.repository.UserRepository;

/**
 * Feature tests for {@code /api/v1/room}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoomControllerFeatureTest {

    private static final String BASE_URL = "/api/v1/room";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RoomRepository roomRepository;

    private User pmAdmin;
    private User baaAdmin;
    private String pmAdminToken;
    private String baaAdminToken;
    private Room existingRoom;

    @BeforeEach
    void setUp() {
        pmAdmin = persistUser(Role.PM, "pmadmin-room");
        pmAdminToken = persistSession(pmAdmin).getId();

        baaAdmin = persistUser(Role.BAA, "baaadmin-room");
        baaAdminToken = persistSession(baaAdmin).getId();

        existingRoom = new Room();
        existingRoom.setName("R201");
        existingRoom.setCapacity(35);
        existingRoom.setCreatedBy(pmAdmin);
        existingRoom.setEditedBy(pmAdmin);
        existingRoom.setCreatedAt(LocalDateTime.now());
        existingRoom.setUpdatedAt(LocalDateTime.now());
        existingRoom = roomRepository.save(existingRoom);
    }

    private User persistUser(Role role, String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("irrelevant-for-these-tests");
        user.setName("Test " + username);
        user.setRole(role);
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
    @DisplayName("GET /room requires a Token header")
    void getRoomsWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /room with a valid token lists the seeded room")
    void getRoomsWithValidTokenListsRooms() throws Exception {
        mockMvc.perform(get(BASE_URL).header("Token", pmAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + existingRoom.getId() + "')]").exists());
    }

    @Test
    @DisplayName("POST /room as PM admin creates a room")
    void createRoomAsPmAdminSucceeds() throws Exception {
        String body = "{\"name\":\"R202\",\"capacity\":25}";

        mockMvc.perform(post(BASE_URL).header("Token", pmAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("R202"))
                .andExpect(jsonPath("$.capacity").value(25));
    }

    @Test
    @DisplayName("POST /room as BAA admin is forbidden (only super admin / PM admin may create rooms)")
    void createRoomAsBaaAdminIsForbidden() throws Exception {
        String body = "{\"name\":\"R203\",\"capacity\":25}";

        mockMvc.perform(post(BASE_URL).header("Token", baaAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @DisplayName("POST /room with capacity below the minimum returns 400")
    void createRoomWithInvalidCapacityReturnsBadRequest() throws Exception {
        String body = "{\"name\":\"Too Small\",\"capacity\":2}";

        mockMvc.perform(post(BASE_URL).header("Token", pmAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Capacity Must Be At Least 5"));
    }

    @Test
    @DisplayName("POST /room with a duplicate name returns 400")
    void createRoomWithDuplicateNameReturnsBadRequest() throws Exception {
        String body = "{\"name\":\"R201\",\"capacity\":25}";

        mockMvc.perform(post(BASE_URL).header("Token", pmAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name Already Exist"));
    }

    @Test
    @DisplayName("POST /room referencing a non-existent lab group returns 400")
    void createRoomWithUnknownLabGroupReturnsBadRequest() throws Exception {
        String body = "{\"name\":\"R204\",\"capacity\":25,\"labGroupId\":\"does-not-exist\"}";

        mockMvc.perform(post(BASE_URL).header("Token", pmAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lab Group ID Not Found"));
    }

    @Test
    @DisplayName("GET /room/{id} for an unknown id returns 404")
    void getRoomByIdNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/does-not-exist").header("Token", pmAdminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Room Not Found"));
    }

    @Test
    @DisplayName("DELETE /room/{id} as PM admin soft deletes the room")
    void deleteRoomAsPmAdminSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + existingRoom.getId()).header("Token", pmAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Room Deleted Successfully"));

        mockMvc.perform(get(BASE_URL + "/" + existingRoom.getId()).header("Token", pmAdminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /room/{id} as BAA admin is forbidden")
    void deleteRoomAsBaaAdminIsForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + existingRoom.getId()).header("Token", baaAdminToken))
                .andExpect(status().isForbidden());
    }
}
