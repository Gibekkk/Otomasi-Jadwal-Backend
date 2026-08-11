package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Schedule;
import com.jadwal.restfulapi.repository.ScheduleRepository;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public Optional<Schedule> findScheduleById(String id) {
        return scheduleRepository.findById(id);
    }

    public List<Schedule> findAllSchedule() {
        return scheduleRepository.findAll();
    }

    public List<Schedule> findAllScheduleById(List<String> scheduleIds) {
        return scheduleRepository.findAllByIdIn(scheduleIds);
    }

    public Optional<Schedule> findScheduleByTimeStart(LocalTime timeStart) {
        return scheduleRepository.findByTimeStart(timeStart);
    }

    public List<String> checkNonExistentSchedules(List<String> scheduleIds) {
        List<Schedule> existingSchedules = findAllScheduleById(scheduleIds);
        scheduleIds.removeIf(id -> existingSchedules.stream().anyMatch(s -> s.getId().equals(id)));
        return scheduleIds;
    }
}