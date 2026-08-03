package com.jadwal.restfulapi.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Notification;
import com.jadwal.restfulapi.model.User;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    public ArrayList<Notification> findByDeletedAtIsNull();
    public ArrayList<Notification> findByDeletedAtIsNullAndUserId(User user);
    public Optional<Notification> findByDeletedAtIsNullAndUserIdAndId(User user, String id);
}
