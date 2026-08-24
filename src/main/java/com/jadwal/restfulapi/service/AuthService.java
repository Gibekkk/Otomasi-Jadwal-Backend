package com.jadwal.restfulapi.service;

import java.util.ArrayList;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Role;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.repository.SessionRepository;
import com.jadwal.restfulapi.repository.UserRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordHasherMatcher passwordMaker;

    private final int SESSION_TIMEOUT_MINUTES = 180;

    @Transactional
    public void deleteSession(Session session) {
        sessionRepository.delete(session);
    }

    public Optional<Session> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsernameAndDeletedAtIsNull(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordMaker.matchPassword(password, user.getPassword())) {
                Session session = regenerateSessionToken(user);
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    public Boolean isSuperAdmin(User user) {
        return user.getRole().equals(Role.SUPERADMIN);
    }

    public Boolean isProdiAdmin(User user) {
        return user.getRole().equals(Role.PRODI);
    }

    public Boolean isBaaAdmin(User user) {
        return user.getRole().equals(Role.BAA);
    }

    public Boolean isNtHumAdmin(User user) {
        return user.getRole().equals(Role.NTHUM);
    }

    public Boolean isPmAdmin(User user) {
        return user.getRole().equals(Role.PM);
    }

    public boolean isExistByUsernameAndIdIsNot(String username, String id) {
        return findUserByUsernameAndIdIsNot(username, id).isPresent();
    }

    public Optional<User> findUserByUsernameAndIdIsNot(String username, String id) {
        return userRepository.findByUsernameAndDeletedAtIsNullAndIdIsNot(username, id);
    }

    public boolean isExistByUsername(String username) {
        return findUserByUsername(username).isPresent();
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username);
    }

    public Optional<User> findUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Session regenerateSessionToken(User user) {
        // Optional<Session> conflictSessionOpt =
        // sessionRepository.findByFcmToken(fcmToken);
        // if(conflictSessionOpt.isPresent()) {
        // Session conflictSession = conflictSessionOpt.get();
        // deleteSession(conflictSession);
        // }
        Session session = new Session();
        session.setUserId(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastSeenAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public Optional<Session> findSessionBySessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            return Optional.empty();
        }
        Optional<Session> sessionOpt = sessionRepository.findById(sessionToken);
        if (sessionOpt.isPresent())
            updateSessionLastSeen(sessionOpt.get());
        return sessionOpt;
    }

    public void updateSessionLastSeen(Session session) {
        session.setLastSeenAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    public void deleteExpiredSessions() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
        ArrayList<Session> expiredSessions = sessionRepository.findByLastSeenAtBefore(expiredTime);
        for (Session session : expiredSessions) {
            deleteSession(session);
        }
    }
}