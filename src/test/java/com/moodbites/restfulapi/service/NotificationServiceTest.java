package com.moodbites.restfulapi.service;

import com.moodbites.restfulapi.model.Notification;
import com.moodbites.restfulapi.model.User;
import com.moodbites.restfulapi.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuthService authService; // Di-mock meskipun belum digunakan di method yang ada

    @Mock
    private FirebaseMessagingService firebaseMessagingService; // Di-mock meskipun belum digunakan

    @InjectMocks
    private NotificationService notificationService;

    private User mockUser;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-123");

        mockNotification = new Notification();
        mockNotification.setId("notif-123"); // Asumsi ID berupa String (UUID)
        mockNotification.setUserId(mockUser);
        mockNotification.setSeenAt(null);
        mockNotification.setDeletedAt(null);
    }

    // =====================================================================
    // getNotificationsByUser()
    // =====================================================================

    @Nested
    @DisplayName("getNotificationsByUser()")
    class GetNotificationsByUser {

        @Test
        @DisplayName("Returns a list of active notifications for a specific user")
        void whenUserHasNotifications_thenReturnsList() {
            ArrayList<Notification> notifications = new ArrayList<>(List.of(mockNotification));
            when(notificationRepository.findByDeletedAtIsNullAndUserId(mockUser))
                    .thenReturn(notifications);

            ArrayList<Notification> result = notificationService.getNotificationsByUser(mockUser);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("notif-123");
            verify(notificationRepository).findByDeletedAtIsNullAndUserId(mockUser);
        }
    }

    // =====================================================================
    // setNotificationRead()
    // =====================================================================

    @Nested
    @DisplayName("setNotificationRead()")
    class SetNotificationRead {

        @Test
        @DisplayName("Sets seenAt to current time and saves when seenAt is initially null")
        void whenSeenAtIsNull_thenUpdatesAndSaves() {
            notificationService.setNotificationRead(mockNotification);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification savedNotification = captor.getValue();
            assertThat(savedNotification.getSeenAt()).isNotNull();
        }

        @Test
        @DisplayName("Does not call save repository when notification is already read")
        void whenSeenAtIsNotNull_thenDoesNothing() {
            mockNotification.setSeenAt(LocalDateTime.now().minusDays(1)); // Sudah terbaca

            notificationService.setNotificationRead(mockNotification);

            // Repository TIDAK boleh dipanggil untuk menghemat operasi database
            verify(notificationRepository, never()).save(any(Notification.class));
        }
    }

    // =====================================================================
    // deleteNotification()
    // =====================================================================

    @Nested
    @DisplayName("deleteNotification()")
    class DeleteNotification {

        @Test
        @DisplayName("Sets deletedAt to current time and saves when deletedAt is initially null (Soft Delete)")
        void whenDeletedAtIsNull_thenUpdatesAndSaves() {
            notificationService.deleteNotification(mockNotification);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification savedNotification = captor.getValue();
            assertThat(savedNotification.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Does not call save repository when notification is already deleted")
        void whenDeletedAtIsNotNull_thenDoesNothing() {
            mockNotification.setDeletedAt(LocalDateTime.now().minusDays(1)); // Sudah terhapus

            notificationService.deleteNotification(mockNotification);

            // Repository TIDAK boleh dipanggil
            verify(notificationRepository, never()).save(any(Notification.class));
        }
    }

    // =====================================================================
    // getAllNotification()
    // =====================================================================

    @Nested
    @DisplayName("getAllNotification()")
    class GetAllNotification {

        @Test
        @DisplayName("Returns all non-deleted notifications in the system")
        void whenCalled_thenReturnsAllActiveNotifications() {
            Notification notif2 = new Notification();
            notif2.setId("notif-456");
            ArrayList<Notification> notifications = new ArrayList<>(List.of(mockNotification, notif2));
            
            when(notificationRepository.findByDeletedAtIsNull()).thenReturn(notifications);

            ArrayList<Notification> result = notificationService.getAllNotification();

            assertThat(result).hasSize(2);
            verify(notificationRepository).findByDeletedAtIsNull();
        }
    }

    // =====================================================================
    // getNotificationByUserAndId()
    // =====================================================================

    @Nested
    @DisplayName("getNotificationByUserAndId()")
    class GetNotificationByUserAndId {

        @Test
        @DisplayName("Returns wrapped notification when it belongs to user and is not deleted")
        void whenNotificationExists_thenReturnsOptionalNotification() {
            when(notificationRepository.findByDeletedAtIsNullAndUserIdAndId(mockUser, "notif-123"))
                    .thenReturn(Optional.of(mockNotification));

            Optional<Notification> result = notificationService.getNotificationByUserAndId(mockUser, "notif-123");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("notif-123");
        }

        @Test
        @DisplayName("Returns empty optional when notification does not exist or is deleted")
        void whenNotificationDoesNotExist_thenReturnsEmptyOptional() {
            when(notificationRepository.findByDeletedAtIsNullAndUserIdAndId(mockUser, "notif-999"))
                    .thenReturn(Optional.empty());

            Optional<Notification> result = notificationService.getNotificationByUserAndId(mockUser, "notif-999");

            assertThat(result).isEmpty();
        }
    }
}