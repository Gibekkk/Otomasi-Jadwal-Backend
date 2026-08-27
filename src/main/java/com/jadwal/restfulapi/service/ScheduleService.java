package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    /**
     * Semua slot jadwal diurutkan berdasarkan timeStart (bukan berdasarkan id/UUID-nya).
     * Dipakai sebagai dasar "urutan slot ke berapa" karena tabel schedules tidak
     * punya kolom urutan eksplisit.
     */
    public List<Schedule> findAllScheduleSortedByTimeStart() {
        return scheduleRepository.findAll().stream()
                .sorted(Comparator.comparing(Schedule::getTimeStart))
                .collect(Collectors.toList());
    }

    /**
     * Peta id slot -> posisi urutannya di dalam list yang sudah terurut berdasarkan timeStart.
     * Bangun sekali lalu pakai berulang kali (mis. di dalam loop) supaya tidak sort ulang tiap iterasi.
     */
    public Map<String, Integer> buildScheduleOrderIndex(List<Schedule> sortedSchedules) {
        Map<String, Integer> orderIndex = new HashMap<>();
        for (int i = 0; i < sortedSchedules.size(); i++) {
            orderIndex.put(sortedSchedules.get(i).getId(), i);
        }
        return orderIndex;
    }

    /**
     * Menentukan timeEnd suatu mata kuliah berdasarkan slot awalnya (startSchedule) dan
     * berapa banyak slot yang dipakai (sksCount). Misal startSchedule ada di urutan ke-4
     * dan sksCount 3, maka dipakai slot 4, 5, 6 -- timeEnd yang dikembalikan adalah
     * timeEnd dari slot ke-6.
     */
    public LocalTime resolveTimeEnd(List<Schedule> sortedSchedules, Map<String, Integer> orderIndex,
            Schedule startSchedule, int sksCount, Boolean isLab) {
        Integer startIndex = orderIndex.get(startSchedule.getId());
        if (startIndex == null) {
            throw new IllegalStateException("Schedule Slot Not Found: " + startSchedule.getId());
        }

        if(isLab) sksCount = sksCount + 2;
        int endIndex = startIndex + sksCount - 1;
        if (endIndex >= sortedSchedules.size()) {
            throw new IllegalStateException(
                    "SKS Count (" + sksCount + ") Exceeds The Schedule Slots " + startSchedule.getTimeStart());
        }

        return sortedSchedules.get(endIndex).getTimeEnd();
    }

    public List<Schedule> findAllScheduleById(List<String> scheduleIds) {
        return scheduleRepository.findAllByIdIn(scheduleIds);
    }

    public Optional<Schedule> findScheduleByTimeStart(LocalTime timeStart) {
        return scheduleRepository.findByTimeStart(timeStart);
    }

    public List<String> checkNonExistentSchedules(List<String> scheduleIds) {
        List<Schedule> existingSchedules = findAllScheduleById(scheduleIds);

        // Buat list baru yang berisi elemen dari list utama
        List<String> schedulesToCheck = new ArrayList<>(scheduleIds);

        schedulesToCheck.removeIf(id -> existingSchedules.stream().anyMatch(s -> s.getId().equals(id)));
        return schedulesToCheck;
    }
}