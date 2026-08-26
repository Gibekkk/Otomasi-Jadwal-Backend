package com.jadwal.restfulapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Lecture;
import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.repository.LectureRepository;
import com.jadwal.restfulapi.repository.FreeTableRepository;
import com.jadwal.restfulapi.repository.TimelineGenerationRepository;

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
        return lectureRepository.findAllByTimelineGenerationId(freeTable.getTimelineGenerationId());
    }
}
