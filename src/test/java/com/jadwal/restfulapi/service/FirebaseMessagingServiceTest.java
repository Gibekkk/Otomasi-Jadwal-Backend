package com.jadwal.restfulapi.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jadwal.restfulapi.dto.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FirebaseMessagingService Unit Tests")
class FirebaseMessagingServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FirebaseMessagingService firebaseMessagingService;

    private NotificationMessage mockNotification;

    @BeforeEach
    void setUp() {
        // Melakukan mock pada DTO untuk menghindari dependensi pada cara DTO diinisialisasi
        mockNotification = mock(NotificationMessage.class);
        
        when(mockNotification.getTitle()).thenReturn("Promo Spesial");
        when(mockNotification.getBody()).thenReturn("Diskon 50% untuk kamu!");
        when(mockNotification.getNotificationType()).thenReturn("PROMO");
        when(mockNotification.getRecipientToken()).thenReturn("fcm-device-token-123");

        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
        additionalData.put("id", "101");
        when(mockNotification.getData()).thenReturn(additionalData);
    }

    // =====================================================================
    // sendNotificationByToken()
    // =====================================================================

    @Nested
    @DisplayName("sendNotificationByToken()")
    class SendNotificationByToken {

        @Test
        @DisplayName("Returns success string when Firebase successfully sends the message")
        void whenSendSuccess_thenReturnsSuccessMessage() throws FirebaseMessagingException {
            // Act
            String result = firebaseMessagingService.sendNotificationByToken(mockNotification);

            // Assert
            assertThat(result).isEqualTo("Notifikasi Berhasil Terkirim");
            
            // Verifikasi bahwa firebaseMessaging.send() benar-benar dipanggil satu kali
            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(firebaseMessaging).send(messageCaptor.capture());
            
            // (Opsional) Kamu juga bisa mengekstrak data dari captured message jika diperlukan,
            // namun kelas Message dari Firebase tidak menyediakan getter publik yang mudah untuk datanya.
            // Memastikan method send() terpanggil dengan object Message saja sudah cukup valid di sini.
            assertThat(messageCaptor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("Catches FirebaseMessagingException and returns failure string")
        void whenSendFails_thenReturnsFailureMessage() throws FirebaseMessagingException {
            // Arrange
            // Melakukan mock pada FirebaseMessagingException karena class ini memiliki
            // constructor yang cukup kompleks di beberapa versi SDK.
            FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
            
            // Simulasikan error saat method send() dipanggil
            when(firebaseMessaging.send(any(Message.class))).thenThrow(mockException);

            // Act
            String result = firebaseMessagingService.sendNotificationByToken(mockNotification);

            // Assert
            assertThat(result).isEqualTo("Notifikasi Gagal Dikirim");
            verify(firebaseMessaging).send(any(Message.class));
            // Stacktrace akan tercetak di console selama testing karena e.printStackTrace(),
            // ini adalah ekspektasi yang wajar dari kode aslinya.
        }
    }
}