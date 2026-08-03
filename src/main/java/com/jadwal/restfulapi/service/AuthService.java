package com.jadwal.restfulapi.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.dto.ProfileDTO;
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

    private final int SESSION_TIMEOUT_MINUTES = 60;

    @Transactional
    public void deleteSession(Session session) {
        sessionRepository.delete(session);
    }

    public Optional<Session> authenticateUser(String email, String password, String fcmToken) {
        Optional<User> userOpt = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getVerifiedAt() != null) {
                if (passwordMaker.matchPassword(password, user.getPassword())) {
                    Session session = regenerateSessionToken(user, fcmToken);
                    return Optional.of(session);
                }
            }
        }
        return Optional.empty();
    }

    public boolean isEmailAvailable(String email) {
        Optional<User> userOpt = userRepository.findByEmailAndDeletedAtIsNull(email);
        return userOpt.isEmpty();
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email);
    }

    public Optional<User> findUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Session regenerateSessionToken(User user, String fcmToken) {
        // Optional<Session> conflictSessionOpt =
        // sessionRepository.findByFcmToken(fcmToken);
        // if(conflictSessionOpt.isPresent()) {
        // Session conflictSession = conflictSessionOpt.get();
        // deleteSession(conflictSession);
        // }
        Session session = new Session();
        session.setUserId(user);
        session.setToken(UUID.randomUUID().toString());
        session.setFcmToken(fcmToken);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastSeenAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public User registerUser(String email, String password, String fcmToken, String name) {
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(passwordMaker.hashPassword(password));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setEditedAt(LocalDateTime.now());
        newUser = userRepository.save(newUser);

        regenerateSessionToken(newUser, fcmToken);
        return newUser;
    }

    public Session verifyUser(User user) {
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
        return user.getSessions().stream().findFirst().orElse(null);
    }

    public Optional<Session> findSessionBySessionToken(String sessionToken) {
        Optional<Session> sessionOpt = sessionRepository.findByToken(sessionToken);
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

    public User editUserProfile(User user, ProfileDTO profileDTO) {
        user.setName(profileDTO.getName());
        user.setEditedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}