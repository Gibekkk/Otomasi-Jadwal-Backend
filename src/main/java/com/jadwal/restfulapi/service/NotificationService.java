package com.jadwal.restfulapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Notification;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.repository.NotificationRepository;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    public ArrayList<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByDeletedAtIsNullAndUserId(user);
    }

    public void setNotificationRead(Notification notification) {
        if(notification.getSeenAt() == null) {
            notification.setSeenAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    public void deleteNotification(Notification notification) {
        if(notification.getDeletedAt() == null) {
            notification.setDeletedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    public ArrayList<Notification> getAllNotification() {
        return notificationRepository.findByDeletedAtIsNull();
    }

    public Optional<Notification> getNotificationByUserAndId(User user, String idNotification) {
        return notificationRepository.findByDeletedAtIsNullAndUserIdAndId(user, idNotification);
    }
}


