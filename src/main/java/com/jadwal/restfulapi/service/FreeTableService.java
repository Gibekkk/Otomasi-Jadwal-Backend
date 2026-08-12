package com.jadwal.restfulapi.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.repository.FreeTableRepository;

@Service
public class FreeTableService {

    @Autowired
    private FreeTableRepository freeTableRepository;

    public Optional<FreeTable> findStatus() {
        return freeTableRepository.findFirstByOrderByIdAsc();
    }

    public FreeTable toggleGenerating(FreeTable freeTable) {
        freeTable.setIsGenerating(!freeTable.getIsGenerating());
        return freeTableRepository.save(freeTable);
    }
}
