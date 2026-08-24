package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.dto.GenerateDTO;
import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.model.TimelineGeneration;
import com.jadwal.restfulapi.repository.FreeTableRepository;
import com.jadwal.restfulapi.repository.TimelineGenerationRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

@Service
public class FreeTableService {

    @Autowired
    private FreeTableRepository freeTableRepository;

    @Autowired
    private TimelineGenerationRepository timelineGenerationRepository;

    @Autowired
    private AlgorithmService algorithmService;

    @Autowired
    private PasswordHasherMatcher passwordMaker;

    public Optional<FreeTable> findStatus() {
        return freeTableRepository.findFirstByOrderByIdAsc();
    }

    public FreeTable startGenerating(FreeTable freeTable, GenerateDTO generateDTO, User user) {
        TimelineGeneration timelineGeneration = new TimelineGeneration();
        timelineGeneration.setAcademicYear(generateDTO.getAcademicYear());
        timelineGeneration.setGeneratedBy(user);
        timelineGeneration.setIsOdd(generateDTO.getIsOdd());
        TimelineGeneration newTimelineGeneration = timelineGenerationRepository.save(timelineGeneration);

        String secretKey = generateSecretKey();
        freeTable.setIsGenerating(true);
        freeTable.setTimelineGenerationId(newTimelineGeneration);
        freeTable.setSecretKey(secretKey);
        FreeTable updatedFreeTable = freeTableRepository.save(freeTable);
        algorithmService.triggerStartGenerate(secretKey);
        return updatedFreeTable;
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



    // Only for testing, delete ASAP
    public FreeTable toggleGenerating(FreeTable freeTable) {
        freeTable.setIsGenerating(!freeTable.getIsGenerating());
        return freeTableRepository.save(freeTable);
    }
}
