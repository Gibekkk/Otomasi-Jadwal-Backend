package com.jadwal.restfulapi.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class SchedulingService {

    @Autowired
    private AuthService authService;
    
    @Scheduled(fixedRate = 5000)
    public void doSomethingEvery5Secs() {
        // authService.deleteExpiredSessions();
    }
}