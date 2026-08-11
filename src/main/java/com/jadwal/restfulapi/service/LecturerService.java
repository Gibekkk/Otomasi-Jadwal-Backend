package com.jadwal.restfulapi.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.model.LecturerSpecialization;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Religion;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.dto.LecturerDTO;
import com.jadwal.restfulapi.repository.LecturerRepository;
import com.jadwal.restfulapi.repository.LecturerSpecializationRepository;

import jakarta.transaction.Transactional;

@Service
public class LecturerService {

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private LecturerSpecializationRepository lecturerSpecializationRepository;

    public Optional<Lecturer> findLecturerById(String id) {
        return lecturerRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<Lecturer> findLecturerByIdAndCategory(String id, Category category) {
        return lecturerRepository.findByIdAndDeletedAtIsNull(id)
                .filter(lecturer -> lecturer.getCategoryId().equals(category));
    }

    public Optional<Lecturer> findLecturerByIdAndCategoryAndInterdicipline(String id, Category category) {
        return lecturerRepository.findByIdAndDeletedAtIsNull(id)
                .filter(lecturer -> lecturer.getCategoryId().equals(category) || lecturer.getIsInterdicipline());
    }

    public List<Lecturer> findLecturerByCategoryAndInterdicipline(Category category) {
        return lecturerRepository.findAllByCategoryIdAndDeletedAtIsNull(category)
                .stream()
                .filter(lecturer -> lecturer.getIsInterdicipline() || lecturer.getCategoryId().equals(category))
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

    public Boolean toggleLecturerActive(Lecturer lecturer) {
        lecturer.setIsActive(!lecturer.getIsActive());
        lecturerRepository.save(lecturer);
        return lecturer.getIsActive();
    }

    public Lecturer createLecturer(LecturerDTO lecturerDTO, Category category, User admin, List<Specialization> specializations) {
        Lecturer lecturer = new Lecturer();
        lecturer.setName(lecturerDTO.getName());
        lecturer.setIsDlb(lecturerDTO.getIsDlb());
        lecturer.setIsMale(lecturerDTO.getIsMale());
        lecturer.setIsInterdicipline(lecturerDTO.getIsInterdicipline());
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

        return savedLecturer;
    }

    public Lecturer editLecturer(Lecturer editedLecturer, LecturerDTO lecturerDTO, Category category, User admin, List<Specialization> specializations) {
        editedLecturer.setName(lecturerDTO.getName());
        editedLecturer.setIsDlb(lecturerDTO.getIsDlb());
        editedLecturer.setIsMale(lecturerDTO.getIsMale());
        editedLecturer.setIsInterdicipline(lecturerDTO.getIsInterdicipline());
        editedLecturer.setReligion(Religion.fromString(lecturerDTO.getReligion()));
        editedLecturer.setCategoryId(category);
        editedLecturer.setEditedBy(admin);
        editedLecturer.setUpdatedAt(LocalDateTime.now());
        Lecturer savedLecturer = lecturerRepository.save(editedLecturer);

        deleteLecturerSpecializationsByLecturer(savedLecturer);
        for (Specialization specialization : specializations) {
            lecturerSpecializationRepository.save(new LecturerSpecialization(null, savedLecturer, specialization));
        }

        return savedLecturer;
    }

    @Transactional
    public void deleteLecturerSpecializationsByLecturer(Lecturer lecturer) {
        lecturerSpecializationRepository.deleteAllByLecturerId(lecturer);
    }
}