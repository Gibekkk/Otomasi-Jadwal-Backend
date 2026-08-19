package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.repository.FreeTableRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

@Service
public class FreeTableService {

    @Autowired
    private FreeTableRepository freeTableRepository;

    @Autowired
    private PasswordHasherMatcher passwordMaker;

    public Optional<FreeTable> findStatus() {
        return freeTableRepository.findFirstByOrderByIdAsc();
    }

    public FreeTable startGenerating(FreeTable freeTable) {
        freeTable.setIsGenerating(true);
        freeTable.setSecretKey(generateSecretKey());
        return freeTableRepository.save(freeTable);
    }

    public FreeTable stopGenerating(FreeTable freeTable) {
        freeTable.setIsGenerating(false);
        freeTable.setSecretKey(null);
        return freeTableRepository.save(freeTable);
    }

    public String generateSecretKey() {
        String randomUUID = UUID.randomUUID().toString();
        String secretKey = passwordMaker.hashPassword(randomUUID);
        return secretKey;
    }
}
