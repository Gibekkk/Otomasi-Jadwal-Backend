package com.jadwal.restfulapi.controller;

import com.google.firebase.messaging.FirebaseMessaging;
import com.jadwal.restfulapi.model.Notification;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.service.NotificationService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass filter security
@TestPropertySource(properties = "storage.api-prefix=/api") // Mock path prefix
@DisplayName("NotificationController Web Layer Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private NotificationService notificationService;

    // Tambahan MockBean untuk mencegah crash akibat dependensi Firebase
    @MockBean
    private FirebaseMessaging firebaseMessaging;

    private final String BASE_URL = "/api/notification";
    private User mockUser;
    private Session mockSession;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-uuid-123");

        mockSession = new Session();
        mockSession.setToken("valid-session-token");
        mockSession.setUserId(mockUser);

        mockNotification = new Notification();
        mockNotification.setId("notif-123");
        mockNotification.setTitle("System Update");
        mockNotification.setContent("Server maintenance tonight.");
        mockNotification.setCreatedAt(LocalDateTime.now());
        mockNotification.setSeenAt(null); // Default belum dibaca
    }

    // =====================================================================
    // GET /
    // =====================================================================
    @Nested
    @DisplayName("GET / (getNotifications)")
    class GetNotifications {

        @Test
        @DisplayName("Returns 200 OK with list of mapped notifications when token is valid")
        void whenValidToken_thenReturns200AndList() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenReturn(Optional.of(mockSession));
            
            ArrayList<Notification> notifications = new ArrayList<>(List.of(mockNotification));
            when(notificationService.getNotificationsByUser(mockUser)).thenReturn(notifications);

            mockMvc.perform(get(BASE_URL)
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value("notif-123"))
                    .andExpect(jsonPath("$[0].title").value("System Update"))
                    .andExpect(jsonPath("$[0].content").value("Server maintenance tonight."))
                    .andExpect(jsonPath("$[0].createdAt").exists());
        }

        @Test
        @DisplayName("Returns 403 FORBIDDEN when session token is invalid")
        void whenInvalidToken_thenReturns403() throws Exception {
            when(authService.findSessionBySessionToken("invalid-token"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL)
                            .header("Token", "invalid-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Authentication Failed"));
        }
    }

    // =====================================================================
    // DELETE /{notificationId}
    // =====================================================================
    @Nested
    @DisplayName("DELETE /{notificationId} (deleteNotification)")
    class DeleteNotification {

        @Test
        @DisplayName("Returns 200 OK and deletes notification when valid token and notification exists")
        void whenValidRequest_thenReturns200() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenReturn(Optional.of(mockSession));
            when(notificationService.getNotificationByUserAndId(mockUser, "notif-123"))
                    .thenReturn(Optional.of(mockNotification));

            mockMvc.perform(delete(BASE_URL + "/notif-123")
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Notification Deleted Successfully"));

            verify(notificationService).deleteNotification(mockNotification);
        }

        @Test
        @DisplayName("Returns 404 NOT FOUND when notification does not exist for the user")
        void whenNotificationNotFound_thenReturns404() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenReturn(Optional.of(mockSession));
            when(notificationService.getNotificationByUserAndId(mockUser, "ghost-notif"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(delete(BASE_URL + "/ghost-notif")
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Notification Not Found"));

            verify(notificationService, never()).deleteNotification(any());
        }
    }

    // =====================================================================
    // GET /{notificationId}
    // =====================================================================
    @Nested
    @DisplayName("GET /{notificationId} (getNotificationById)")
    class GetNotificationById {

        @Test
        @DisplayName("Returns 200 OK, retrieves notification, and marks it as read")
        void whenValidRequest_thenReturns200AndMarksAsRead() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenReturn(Optional.of(mockSession));
            when(notificationService.getNotificationByUserAndId(mockUser, "notif-123"))
                    .thenReturn(Optional.of(mockNotification));

            mockMvc.perform(get(BASE_URL + "/notif-123")
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("notif-123"))
                    .andExpect(jsonPath("$.title").value("System Update"));

            // Memastikan method setNotificationRead benar-benar dipanggil (side-effect validation)
            verify(notificationService).setNotificationRead(mockNotification);
        }

        @Test
        @DisplayName("Returns 404 NOT FOUND when notification does not exist for the user")
        void whenNotificationNotFound_thenReturns404() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenReturn(Optional.of(mockSession));
            when(notificationService.getNotificationByUserAndId(mockUser, "ghost-notif"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/ghost-notif")
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Notification Not Found"));

            verify(notificationService, never()).setNotificationRead(any());
        }

        @Test
        @DisplayName("Returns 500 INTERNAL SERVER ERROR for unexpected exceptions")
        void whenUnexpectedException_thenReturns500() throws Exception {
            when(authService.findSessionBySessionToken("valid-session-token"))
                    .thenThrow(new RuntimeException("Database error simulation"));

            mockMvc.perform(get(BASE_URL + "/notif-123")
                            .header("Token", "valid-session-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Database error simulation"));
        }
    }
}