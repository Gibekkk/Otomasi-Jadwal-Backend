package com.jadwal.restfulapi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Lecture;
import java.util.Optional;
import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.model.CourseSchedule;
import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.repository.LectureRepository;
import com.jadwal.restfulapi.repository.FreeTableRepository;

@Service
public class TimelineService {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private FreeTableRepository freeTableRepository;

    public List<Lecture> getLectures() {
        FreeTable freeTable = freeTableRepository.findFirstByOrderByIdAsc().get();
        return lectureRepository.findAllByTimelineGenerationId(freeTable.getTimelineGenerationId());
    }

    public Optional<Lecture> getLectureById(String lectureId) {
        return lectureRepository.findById(lectureId);
    }

    public ArrayList<Lecture> getLectureSplitsByLecture(Lecture pointedLecture) {
        FreeTable freeTable = freeTableRepository.findFirstByOrderByIdAsc().get();
        List<Lecture> lectures = lectureRepository
                .findAllByTimelineGenerationIdAndIdNot(freeTable.getTimelineGenerationId(), pointedLecture.getId());
        ArrayList<Lecture> lectureSplits = new ArrayList<Lecture>();
        for (Lecture lecture : lectures) {
            CourseSchedule courseSchedule = lecture.getCourseScheduleId();
            Course course = courseSchedule.getCourseId();
            if (course.getId().equals(pointedLecture.getCourseScheduleId().getCourseId().getId())
                    && courseSchedule.getCourseIndex().equals(pointedLecture.getCourseScheduleId().getCourseIndex())
                    && !courseSchedule.getIsLab()) {
                lectureSplits.add(lecture);
            }
        }
        return lectureSplits;
    }
}
