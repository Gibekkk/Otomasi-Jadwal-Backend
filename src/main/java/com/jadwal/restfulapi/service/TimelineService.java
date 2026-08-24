package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.dto.GenerateDTO;
import com.jadwal.restfulapi.model.Lecture;
import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.model.TimelineGeneration;
import com.jadwal.restfulapi.repository.LectureRepository;
import com.jadwal.restfulapi.repository.FreeTableRepository;
import com.jadwal.restfulapi.repository.TimelineGenerationRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

@Service
public class TimelineService {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private FreeTableRepository freeTableRepository;

    @Autowired
    private TimelineGenerationRepository timelineGenerationRepository;

    public List<Lecture> getLectures() {
        FreeTable freeTable = freeTableRepository.findFirstByOrderByIdAsc().get();
        return lectureRepository.findAllByTimelineGenerationid(freeTable.getTimelineGenerationId());
    }
}
