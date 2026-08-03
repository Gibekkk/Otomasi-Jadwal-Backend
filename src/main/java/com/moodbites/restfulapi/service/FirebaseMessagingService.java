package com.moodbites.restfulapi.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moodbites.restfulapi.dto.NotificationMessage;

@Service
public class FirebaseMessagingService {
    @Autowired
    private FirebaseMessaging firebaseMessaging;

    public String sendNotificationByToken(NotificationMessage notificationMessage) {
        Message message = Message
                .builder()
                .putData("title", notificationMessage.getTitle())
                .putData("body", notificationMessage.getBody())
                .putData("type", notificationMessage.getNotificationType())
                .putAllData(notificationMessage.getData())
                .setToken(notificationMessage.getRecipientToken())
                .build();

        try {
            firebaseMessaging.send(message);
            return "Notifikasi Berhasil Terkirim";
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Notifikasi Gagal Dikirim";
        }
    }
}