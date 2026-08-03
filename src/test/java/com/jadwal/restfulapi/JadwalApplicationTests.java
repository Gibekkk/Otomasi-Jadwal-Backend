package com.jadwal.restfulapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

@SpringBootTest
@MockBean(GoogleCredentials.class)
@MockBean(FirebaseApp.class)
// PERBAIKAN: Menambahkan MockBean untuk FirebaseMessaging agar contextLoads tidak crash
@MockBean(FirebaseMessaging.class)
class JadwalApplicationTests {
    
    @Test
    void contextLoads() {
    }
}