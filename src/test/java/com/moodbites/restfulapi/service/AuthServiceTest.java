package com.moodbites.restfulapi.service;

import com.moodbites.restfulapi.dto.ProfileDTO;
import com.moodbites.restfulapi.model.Session;
import com.moodbites.restfulapi.model.User;
import com.moodbites.restfulapi.repository.SessionRepository;
import com.moodbites.restfulapi.repository.UserRepository;
import com.moodbites.restfulapi.util.PasswordHasherMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordHasherMatcher passwordMaker;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Session testSession;
    private final String email = "test@moodbites.com";
    private final String rawPassword = "password123";
    private final String hashedPassword = "hashedPassword123";
    private final String fcmToken = "fcm-token-test";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setEmail(email);
        testUser.setPassword(hashedPassword);
        testUser.setName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setSessions(new HashSet<>());

        testSession = new Session();
        testSession.setId(UUID.randomUUID().toString());
        testSession.setUserId(testUser);
        testSession.setToken(UUID.randomUUID().toString());
        testSession.setFcmToken(fcmToken);
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setLastSeenAt(LocalDateTime.now());
    }

    // --- authenticateUser ---

    @Test
    void authenticateUser_ValidCredentials_ReturnsSession() {
        testUser.setVerifiedAt(LocalDateTime.now());
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(testUser));
        when(passwordMaker.matchPassword(rawPassword, hashedPassword)).thenReturn(true);
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Session> result = authService.authenticateUser(email, rawPassword, fcmToken);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(testUser);
        assertThat(result.get().getFcmToken()).isEqualTo(fcmToken);
        assertThat(result.get().getToken()).isNotNull();
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void authenticateUser_WrongPassword_ReturnsEmpty() {
        testUser.setVerifiedAt(LocalDateTime.now());
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(testUser));
        when(passwordMaker.matchPassword(rawPassword, hashedPassword)).thenReturn(false);

        Optional<Session> result = authService.authenticateUser(email, rawPassword, fcmToken);

        assertThat(result).isEmpty();
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void authenticateUser_NotVerified_ReturnsEmpty() {
        testUser.setVerifiedAt(null);
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(testUser));

        Optional<Session> result = authService.authenticateUser(email, rawPassword, fcmToken);

        assertThat(result).isEmpty();
        verify(passwordMaker, never()).matchPassword(anyString(), anyString());
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void authenticateUser_EmailNotFound_ReturnsEmpty() {
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.empty());

        Optional<Session> result = authService.authenticateUser(email, rawPassword, fcmToken);

        assertThat(result).isEmpty();
    }

    // --- isEmailAvailable ---

    @Test
    void isEmailAvailable_EmailNotExists_ReturnsTrue() {
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.empty());
        boolean result = authService.isEmailAvailable(email);
        assertThat(result).isTrue();
    }

    @Test
    void isEmailAvailable_EmailExists_ReturnsFalse() {
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(testUser));
        boolean result = authService.isEmailAvailable(email);
        assertThat(result).isFalse();
    }

    // --- registerUser ---

    @Test
    void registerUser_ValidData_SavesUserAndSession() {
        String name = "New User";
        when(passwordMaker.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(email, rawPassword, fcmToken, name);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(hashedPassword);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(userRepository, times(1)).save(any(User.class));
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    // --- verifyUser ---

    @Test
    void verifyUser_SetsVerifiedAtAndReturnsSession() {
        testUser.getSessions().add(testSession);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Session result = authService.verifyUser(testUser);

        assertThat(testUser.getVerifiedAt()).isNotNull();
        assertThat(result).isEqualTo(testSession);
        verify(userRepository, times(1)).save(testUser);
    }

    // --- findSessionBySessionToken ---

    @Test
    void findSessionBySessionToken_ValidToken_UpdatesLastSeenAndReturnsSession() {
        String token = testSession.getToken();
        LocalDateTime oldLastSeen = testSession.getLastSeenAt();
        when(sessionRepository.findByToken(token)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Optional<Session> result = authService.findSessionBySessionToken(token);

        assertThat(result).isPresent();
        assertThat(result.get().getLastSeenAt()).isAfterOrEqualTo(oldLastSeen);
        verify(sessionRepository, times(1)).save(testSession);
    }

    @Test
    void findSessionBySessionToken_InvalidToken_ReturnsEmpty() {
        String invalidToken = "invalid-token";
        when(sessionRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        Optional<Session> result = authService.findSessionBySessionToken(invalidToken);

        assertThat(result).isEmpty();
        verify(sessionRepository, never()).save(any(Session.class));
    }

    // --- deleteSession ---

    @Test
    void deleteSession_CallsRepositoryDelete() {
        authService.deleteSession(testSession);
        verify(sessionRepository, times(1)).delete(testSession);
    }

    // --- editUserProfile ---

    @Test
    void editUserProfile_UpdatesNameAndEditedAt() {
        ProfileDTO dto = new ProfileDTO();
        dto.setName("Updated Name");
        LocalDateTime oldEditedAt = testUser.getEditedAt();
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.editUserProfile(testUser, dto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEditedAt()).isNotNull();
        if (oldEditedAt != null) {
            assertThat(result.getEditedAt()).isAfterOrEqualTo(oldEditedAt);
        }
        verify(userRepository, times(1)).save(testUser);
    }

    // --- deleteExpiredSessions ---

    @Test
    void deleteExpiredSessions_DeletesOnlyExpired() {
        Session expiredSession1 = new Session();
        Session expiredSession2 = new Session();
        ArrayList<Session> expiredList = new ArrayList<>();
        expiredList.add(expiredSession1);
        expiredList.add(expiredSession2);

        when(sessionRepository.findByLastSeenAtBefore(any(LocalDateTime.class))).thenReturn(expiredList);

        authService.deleteExpiredSessions();

        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(sessionRepository, times(1)).findByLastSeenAtBefore(timeCaptor.capture());
        
        // Assert the threshold is around 60 minutes ago
        LocalDateTime capturedTime = timeCaptor.getValue();
        assertThat(capturedTime).isBefore(LocalDateTime.now().minusMinutes(59));

        verify(sessionRepository, times(1)).delete(expiredSession1);
        verify(sessionRepository, times(1)).delete(expiredSession2);
    }
}