package com.moodbites.restfulapi.service;

import com.moodbites.restfulapi.dto.EmailDetails;
import com.moodbites.restfulapi.model.OTP;
import com.moodbites.restfulapi.model.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage realMimeMessage;
    private final String SENDER_EMAIL = "noreply@moodbites.com";

    @BeforeEach
    void setUp() {
        // Mengisi nilai @Value("${spring.mail.username}") secara manual
        ReflectionTestUtils.setField(emailService, "sender", SENDER_EMAIL);

        // Membuat instance MimeMessage asli agar MimeMessageHelper tidak error saat dipanggil
        jakarta.mail.Session mailSession = jakarta.mail.Session.getDefaultInstance(new Properties());
        realMimeMessage = new MimeMessage(mailSession);
    }

    // =====================================================================
    // sendEmail()
    // =====================================================================

    @Nested
    @DisplayName("sendEmail()")
    class SendEmail {

        @Test
        @DisplayName("Successfully creates MimeMessage and calls javaMailSender.send()")
        void whenValidDetails_thenEmailIsSent() throws Exception {
            // Arrange
            when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage);

            EmailDetails details = new EmailDetails();
            details.setRecipient("user@example.com");
            details.setSubject("Test Subject");
            details.setMsgBody("<p>Test Body</p>");

            // Act
            emailService.sendEmail(details);

            // Assert
            verify(javaMailSender).createMimeMessage();
            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(javaMailSender).send(messageCaptor.capture());

            // Memastikan data yang di-set melalui Helper masuk ke dalam MimeMessage
            MimeMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getFrom()[0].toString()).isEqualTo(SENDER_EMAIL);
            assertThat(capturedMessage.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
            assertThat(capturedMessage.getSubject()).isEqualTo("Test Subject");
        }

        @Test
        @DisplayName("Catches exception when sending email fails without throwing it further")
        void whenMailSenderThrowsException_thenItIsCaught() {
            // Arrange
            when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage);
            doThrow(new MailSendException("SMTP Server down")).when(javaMailSender).send(any(MimeMessage.class));

            EmailDetails details = new EmailDetails();
            details.setRecipient("error@example.com");
            
            // PERBAIKAN: Menambahkan Subject agar MimeMessageHelper tidak melempar IllegalArgumentException
            details.setSubject("Error Subject");
            
            // PERBAIKAN: Menambahkan msgBody agar MimeMessageHelper.setText tidak melempar IllegalArgumentException
            details.setMsgBody("<p>Error body for testing</p>");

            // Act & Assert
            emailService.sendEmail(details);
            
            verify(javaMailSender).send(any(MimeMessage.class));
        }
    }

    // =====================================================================
    // sendOTPRegisToLogin()
    // =====================================================================

    @Nested
    @DisplayName("sendOTPRegisToLogin()")
    class SendOTPRegisToLogin {

        @Test
        @DisplayName("Builds HTML correctly and delegates to sendEmail()")
        void whenUserAndOtpProvided_thenHtmlIsBuiltAndSent() throws Exception {
            // Arrange
            when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage);

            User mockUser = new User();
            mockUser.setName("Gibekkk");
            mockUser.setEmail("gibekkk@moodbites.com");

            OTP mockOtp = new OTP();
            mockOtp.setCode("123456");

            // Act
            emailService.sendOTPRegisToLogin(mockUser, mockOtp);

            // Assert
            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(javaMailSender).send(messageCaptor.capture());

            MimeMessage capturedMessage = messageCaptor.getValue();
            
            assertThat(capturedMessage.getAllRecipients()[0].toString()).isEqualTo("gibekkk@moodbites.com");
            assertThat(capturedMessage.getSubject()).isEqualTo("Kode OTP Anda");
            
            // Verifikasi isi HTML dari MimeMessage (mengandung nama dan kode OTP)
            String content = capturedMessage.getContent().toString();
            assertThat(content).contains("Halo Gibekkk");
            assertThat(content).contains("123456");
            assertThat(content).contains("5 menit");
        }
    }
}