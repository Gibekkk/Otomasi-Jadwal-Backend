package com.jadwal.restfulapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Role;
import com.jadwal.restfulapi.repository.SessionRepository;
import com.jadwal.restfulapi.repository.UserRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

/**
 * Unit tests for {@link AuthService}. All collaborators are mocked so these
 * run without a Spring context or database.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordHasherMatcher passwordMaker;

    @InjectMocks
    private AuthService authService;

    private User superAdmin;
    private User prodiAdminWithValidProdi;
    private User prodiAdminWithoutValidProdi;

    @BeforeEach
    void setUp() {
        superAdmin = new User();
        superAdmin.setId("user-superadmin");
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword("hashed-password");
        superAdmin.setRole(Role.SUPERADMIN);

        Category activeProdi = new Category();
        activeProdi.setId("prodi-1");
        activeProdi.setIsProdi(true);
        activeProdi.setDeletedAt(null);

        prodiAdminWithValidProdi = new User();
        prodiAdminWithValidProdi.setId("user-prodi-1");
        prodiAdminWithValidProdi.setUsername("prodiadmin");
        prodiAdminWithValidProdi.setPassword("hashed-password");
        prodiAdminWithValidProdi.setRole(Role.PRODI);
        prodiAdminWithValidProdi.setProdiId(activeProdi);

        prodiAdminWithoutValidProdi = new User();
        prodiAdminWithoutValidProdi.setId("user-prodi-2");
        prodiAdminWithoutValidProdi.setUsername("orphanprodi");
        prodiAdminWithoutValidProdi.setPassword("hashed-password");
        prodiAdminWithoutValidProdi.setRole(Role.PRODI);
        prodiAdminWithoutValidProdi.setProdiId(null);
    }

    @Nested
    @DisplayName("authenticateUser")
    class AuthenticateUser {

        @Test
        @DisplayName("returns a session when username and password are correct")
        void returnsSessionOnValidCredentials() {
            when(userRepository.findByUsernameAndDeletedAtIsNull("superadmin"))
                    .thenReturn(Optional.of(superAdmin));
            when(passwordMaker.matchPassword("plain-password", "hashed-password")).thenReturn(true);
            when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Optional<Session> result = authService.authenticateUser("superadmin", "plain-password");

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(superAdmin);
            verify(sessionRepository, times(1)).save(any(Session.class));
        }

        @Test
        @DisplayName("returns empty when the user does not exist")
        void returnsEmptyWhenUserNotFound() {
            when(userRepository.findByUsernameAndDeletedAtIsNull("ghost")).thenReturn(Optional.empty());

            Optional<Session> result = authService.authenticateUser("ghost", "whatever");

            assertThat(result).isEmpty();
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns empty when the password does not match")
        void returnsEmptyWhenPasswordWrong() {
            when(userRepository.findByUsernameAndDeletedAtIsNull("superadmin"))
                    .thenReturn(Optional.of(superAdmin));
            when(passwordMaker.matchPassword("wrong-password", "hashed-password")).thenReturn(false);

            Optional<Session> result = authService.authenticateUser("superadmin", "wrong-password");

            assertThat(result).isEmpty();
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("blocks a PRODI user whose prodi category is missing or inactive")
        void blocksProdiUserWithoutValidProdi() {
            when(userRepository.findByUsernameAndDeletedAtIsNull("orphanprodi"))
                    .thenReturn(Optional.of(prodiAdminWithoutValidProdi));

            Optional<Session> result = authService.authenticateUser("orphanprodi", "any-password");

            assertThat(result).isEmpty();
            verify(passwordMaker, never()).matchPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("allows a PRODI user with a valid, active prodi category")
        void allowsProdiUserWithValidProdi() {
            when(userRepository.findByUsernameAndDeletedAtIsNull("prodiadmin"))
                    .thenReturn(Optional.of(prodiAdminWithValidProdi));
            when(passwordMaker.matchPassword("plain-password", "hashed-password")).thenReturn(true);
            when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Optional<Session> result = authService.authenticateUser("prodiadmin", "plain-password");

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("role helpers")
    class RoleHelpers {

        @Test
        void isSuperAdminTrueOnlyForSuperAdminRole() {
            assertThat(authService.isSuperAdmin(superAdmin)).isTrue();
            assertThat(authService.isSuperAdmin(prodiAdminWithValidProdi)).isFalse();
        }

        @Test
        void isProdiAdminRequiresRoleAndActiveProdi() {
            assertThat(authService.isProdiAdmin(prodiAdminWithValidProdi)).isTrue();
            assertThat(authService.isProdiAdmin(prodiAdminWithoutValidProdi)).isFalse();
            assertThat(authService.isProdiAdmin(superAdmin)).isFalse();
        }

        @Test
        void isProdiAdminFalseWhenProdiIsSoftDeleted() {
            Category deletedProdi = new Category();
            deletedProdi.setIsProdi(true);
            deletedProdi.setDeletedAt(LocalDateTime.now());

            User user = new User();
            user.setRole(Role.PRODI);
            user.setProdiId(deletedProdi);

            assertThat(authService.isProdiAdmin(user)).isFalse();
        }

        @Test
        void isBaaAdminAndNtHumAdminAndPmAdmin() {
            User baa = new User();
            baa.setRole(Role.BAA);
            User nthum = new User();
            nthum.setRole(Role.NTHUM);
            User pm = new User();
            pm.setRole(Role.PM);

            assertThat(authService.isBaaAdmin(baa)).isTrue();
            assertThat(authService.isNtHumAdmin(nthum)).isTrue();
            assertThat(authService.isPmAdmin(pm)).isTrue();
            assertThat(authService.isBaaAdmin(superAdmin)).isFalse();
        }
    }

    @Nested
    @DisplayName("findSessionBySessionToken")
    class FindSessionBySessionToken {

        @Test
        @DisplayName("returns empty for a null or blank token without touching the repository")
        void returnsEmptyForBlankToken() {
            assertThat(authService.findSessionBySessionToken(null)).isEmpty();
            assertThat(authService.findSessionBySessionToken("  ")).isEmpty();
            verify(sessionRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("refreshes lastSeenAt when the session belongs to a valid role")
        void refreshesLastSeenForValidRole() {
            Session session = new Session();
            session.setId("session-token");
            session.setUserId(superAdmin);
            session.setLastSeenAt(LocalDateTime.now().minusHours(1));

            when(sessionRepository.findById("session-token")).thenReturn(Optional.of(session));
            when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Optional<Session> result = authService.findSessionBySessionToken("session-token");

            assertThat(result).isPresent();
            verify(sessionRepository, times(1)).save(session);
            verify(sessionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes and returns empty when the session's user no longer has a valid role")
        void deletesSessionForInvalidRole() {
            User invalidUser = new User();
            invalidUser.setRole(Role.PRODI);
            invalidUser.setProdiId(null);

            Session session = new Session();
            session.setId("stale-token");
            session.setUserId(invalidUser);

            when(sessionRepository.findById("stale-token")).thenReturn(Optional.of(session));

            Optional<Session> result = authService.findSessionBySessionToken("stale-token");

            assertThat(result).isEmpty();
            verify(sessionRepository, times(1)).delete(session);
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns empty when no session matches the token")
        void returnsEmptyWhenTokenUnknown() {
            when(sessionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThat(authService.findSessionBySessionToken("unknown")).isEmpty();
        }
    }

    @Test
    @DisplayName("deleteExpiredSessions removes every session last seen before the timeout window")
    void deleteExpiredSessionsRemovesStaleSessions() {
        Session expiredOne = new Session();
        expiredOne.setId("expired-1");
        Session expiredTwo = new Session();
        expiredTwo.setId("expired-2");

        ArrayList<Session> expiredSessions = new ArrayList<>();
        expiredSessions.add(expiredOne);
        expiredSessions.add(expiredTwo);

        when(sessionRepository.findByLastSeenAtBefore(any(LocalDateTime.class))).thenReturn(expiredSessions);

        authService.deleteExpiredSessions();

        verify(sessionRepository, times(1)).delete(expiredOne);
        verify(sessionRepository, times(1)).delete(expiredTwo);
    }
}
