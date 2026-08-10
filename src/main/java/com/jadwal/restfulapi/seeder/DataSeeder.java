package com.jadwal.restfulapi.seeder;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.model.Schedule;
import com.jadwal.restfulapi.model.UserGroup;
import com.jadwal.restfulapi.repository.FreeTableRepository;
import com.jadwal.restfulapi.repository.ScheduleRepository;
import com.jadwal.restfulapi.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final UserGroupRepository userGroupRepository;
    private final FreeTableRepository freeTableRepository;

    private static final LocalTime SCHEDULE_START = LocalTime.of(7, 30);
    private static final LocalTime SCHEDULE_END = LocalTime.of(16, 40);
    private static final int SCHEDULE_INTERVAL_MINUTES = 50;

    private static final List<String> USER_GROUP_NAMES = List.of(
            "Super Admin",
            "Prodi Admin",
            "BAA Admin",
            "NTHUM Admin",
            "PM Admin");

    private static final int FREE_TABLE_YEAR = 2026;
    private static final boolean FREE_TABLE_IS_ODD = true;
    private static final boolean FREE_TABLE_IS_GENERATING = false;

    @Override
    public void run(String... args) {
        seedSchedules();
        seedUserGroups();
        seedFreeTable();
    }

    private void seedSchedules() {
        List<LocalTime[]> slots = buildScheduleSlots();
        int inserted = 0;
        for (LocalTime[] slot : slots) {
            if (!scheduleRepository.existsByTimeStart(slot[0])) {
                Schedule schedule = new Schedule();
                schedule.setTimeStart(slot[0]);
                schedule.setTimeEnd(slot[1]);
                scheduleRepository.save(schedule);
                inserted++;
            }
        }
        if (inserted > 0) {
            System.out.println("Schedule seeder: " + inserted + " slot ditambahkan.");
        }
    }

    private List<LocalTime[]> buildScheduleSlots() {
        List<LocalTime[]> slots = new ArrayList<>();
        LocalTime current = SCHEDULE_START;
        while (current.isBefore(SCHEDULE_END)) {
            LocalTime next = current.plusMinutes(SCHEDULE_INTERVAL_MINUTES);
            if (next.isAfter(SCHEDULE_END)) {
                next = SCHEDULE_END;
            }
            slots.add(new LocalTime[] { current, next });
            current = next;
        }
        return slots;
    }

    private void seedUserGroups() {
        int inserted = 0;
        for (String name : USER_GROUP_NAMES) {
            if (userGroupRepository.findByName(name).isEmpty()) {
                UserGroup group = new UserGroup();
                group.setName(name);
                userGroupRepository.save(group);
                inserted++;
            }
        }
        if (inserted > 0) {
            System.out.println("UserGroup seeder: " + inserted + " grup ditambahkan.");
        }
    }

    private void seedFreeTable() {
        boolean exists = freeTableRepository
                .findByAcademicYearAndIsOdd(FREE_TABLE_YEAR, FREE_TABLE_IS_ODD)
                .isPresent();
        if (!exists) {
            FreeTable freeTable = new FreeTable();
            freeTable.setIsGenerating(FREE_TABLE_IS_GENERATING);
            freeTable.setIsOdd(FREE_TABLE_IS_ODD);
            freeTable.setAcademicYear(FREE_TABLE_YEAR);
            freeTableRepository.save(freeTable);
            System.out.println("FreeTable seeder: data ditambahkan.");
        }
    }
}