package com.moodbites.restfulapi.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moodbites.restfulapi.model.Session;


public interface SessionRepository extends JpaRepository<Session, String> {
    public Optional<Session> findByFcmToken(String fcmToken);
    public Optional<Session> findByToken(String token);
    public ArrayList<Session> findByLastSeenAtBefore(LocalDateTime time);
}
