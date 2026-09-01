package com.jadwal.restfulapi.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.LecturerSpecialization;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.Schedule;
import com.jadwal.restfulapi.model.LecturerSchedule;
import com.jadwal.restfulapi.model.LecturerScheduleTime;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Religion;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.dto.LecturerDTO;
import com.jadwal.restfulapi.repository.LecturerRepository;
import com.jadwal.restfulapi.repository.LecturerSpecializationRepository;
import com.jadwal.restfulapi.wrapper.LecturerScheduleWrapper;
import com.jadwal.restfulapi.repository.LecturerScheduleRepository;
import com.jadwal.restfulapi.repository.LecturerScheduleTimeRepository;

@Service
public class LecturerService {

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private LecturerSpecializationRepository lecturerSpecializationRepository;

    @Autowired
    private LecturerScheduleRepository lecturerScheduleRepository;

    @Autowired
    private LecturerScheduleTimeRepository lecturerScheduleTimeRepository;

    public Optional<Lecturer> findLecturerById(String id) {
        return lecturerRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<Lecturer> findLecturerByIdAndCategory(String id, Category category) {
        return lecturerRepository.findByIdAndDeletedAtIsNull(id)
                .filter(lecturer -> lecturer.getCategoryId().equals(category));
    }

    public Optional<Lecturer> findLecturerByIdAndCategoryAndInterdiscipline(String id, Category category) {
        return findLecturerByIdAndCategory(id, category)
                .filter(lecturer -> lecturer.getIsInterdiscipline());
    }

    public List<Lecturer> findLecturerByCategoryAndInterdiscipline(Category category) {
        return findAllLecturer()
                .stream()
                .filter(lecturer -> lecturer.getIsInterdiscipline() || lecturer.getCategoryId().equals(category))
                .toList();
    }

    public List<Lecturer> findLecturerByCategory(Category category) {
        return lecturerRepository.findAllByCategoryIdAndDeletedAtIsNull(category);
    }

    public List<Lecturer> findAllLecturer() {
        return lecturerRepository.findAllByDeletedAtIsNull();
    }

    public void deleteLecturer(Lecturer lecturer) {
        lecturer.setDeletedAt(LocalDateTime.now());
        lecturerRepository.save(lecturer);
    }

    public Boolean makeLecturerActive(Lecturer lecturer, User admin) {
        lecturer.setIsActive(true);
        lecturer.setEditedBy(admin);
        lecturer.setUpdatedAt(LocalDateTime.now());
        lecturerRepository.save(lecturer);
        return lecturer.getIsActive();
    }

    public Boolean makeLecturerInactive(Lecturer lecturer, User admin) {
        lecturer.setIsActive(false);
        lecturer.setEditedBy(admin);
        lecturer.setUpdatedAt(LocalDateTime.now());
        lecturerRepository.save(lecturer);
        return lecturer.getIsActive();
    }

    public Lecturer createLecturer(LecturerDTO lecturerDTO, Category category, User admin,
            List<Specialization> specializations, List<LecturerScheduleWrapper> schedules) {
        Lecturer lecturer = new Lecturer();
        lecturer.setName(lecturerDTO.getName());
        lecturer.setIsMale(lecturerDTO.getIsMale());
        lecturer.setIsInterdiscipline(lecturerDTO.getIsInterdiscipline());
        lecturer.setIsActive(true);
        lecturer.setReligion(Religion.fromString(lecturerDTO.getReligion()));
        lecturer.setCategoryId(category);
        lecturer.setCreatedBy(admin);
        lecturer.setEditedBy(admin);
        lecturer.setCreatedAt(LocalDateTime.now());
        lecturer.setUpdatedAt(LocalDateTime.now());
        Lecturer savedLecturer = lecturerRepository.save(lecturer);

        for (Specialization specialization : specializations) {
            lecturerSpecializationRepository.save(new LecturerSpecialization(null, savedLecturer, specialization));
        }

        for (LecturerScheduleWrapper scheduleWrapper : schedules) {
            LecturerSchedule lecturerSchedule = lecturerScheduleRepository.save(new LecturerSchedule(null, savedLecturer, scheduleWrapper.getDay(), null));
            for (Schedule schedule : scheduleWrapper.getSchedules()) {
                lecturerScheduleTimeRepository.save(new LecturerScheduleTime(null, lecturerSchedule, schedule));
            }
        }

        return savedLecturer;
    }

    public Lecturer editLecturer(Lecturer editedLecturer, LecturerDTO lecturerDTO, Category category, User admin,
            List<Specialization> specializations, List<LecturerScheduleWrapper> schedules) {
        editedLecturer.setName(lecturerDTO.getName());
        editedLecturer.setIsMale(lecturerDTO.getIsMale());
        editedLecturer.setIsInterdiscipline(lecturerDTO.getIsInterdiscipline());
        editedLecturer.setReligion(Religion.fromString(lecturerDTO.getReligion()));
        editedLecturer.setCategoryId(category);
        editedLecturer.setEditedBy(admin);
        editedLecturer.setUpdatedAt(LocalDateTime.now());
        Lecturer savedLecturer = lecturerRepository.save(editedLecturer);

        deleteLecturerSpecializationsByLecturer(savedLecturer);
        for (Specialization specialization : specializations) {
            lecturerSpecializationRepository.save(new LecturerSpecialization(null, savedLecturer, specialization));
        }
        
        for (LecturerSchedule lecturerSchedule : savedLecturer.getLecturerSchedules()) {
            deleteLecturerScheduleTimesByLecturerSchedule(lecturerSchedule);
        }
        deleteLecturerSchedulesByLecturer(savedLecturer);
        for (LecturerScheduleWrapper scheduleWrapper : schedules) {
            LecturerSchedule lecturerSchedule = lecturerScheduleRepository.save(new LecturerSchedule(null, savedLecturer, scheduleWrapper.getDay(), null));
            for (Schedule schedule : scheduleWrapper.getSchedules()) {
                lecturerScheduleTimeRepository.save(new LecturerScheduleTime(null, lecturerSchedule, schedule));
            }
        }
        
        return savedLecturer;
    }

    public void deleteLecturerSpecializationsByLecturer(Lecturer lecturer) {
        lecturerSpecializationRepository.deleteAllByLecturerId(lecturer);
    }

    public void deleteLecturerSchedulesByLecturer(Lecturer lecturer) {
        lecturerScheduleRepository.deleteAllByLecturerId(lecturer);
    }

    public void deleteLecturerScheduleTimesByLecturerSchedule(LecturerSchedule lecturerSchedule) {
        lecturerScheduleTimeRepository.deleteAllByLecturerScheduleId(lecturerSchedule);
    }
}