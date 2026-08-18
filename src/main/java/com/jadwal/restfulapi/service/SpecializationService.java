package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.dto.SpecializationDTO;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.repository.CourseSpecializationRepository;
import com.jadwal.restfulapi.repository.LabSpecializationRepository;
import com.jadwal.restfulapi.repository.LecturerSpecializationRepository;
import com.jadwal.restfulapi.repository.SpecializationRepository;

import jakarta.transaction.Transactional;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private LecturerSpecializationRepository lecturerSpecializationRepository;

    @Autowired
    private CourseSpecializationRepository courseSpecializationRepository;

    @Autowired
    private LabSpecializationRepository labSpecializationRepository;

    public Optional<Specialization> findSpecializationById(String id) {
        return specializationRepository.findByIdAndDeletedAtIsNull(id);
    }
    
    public Boolean isSpecializationExistByName(String name) {
        return specializationRepository.existsByNameAndDeletedAtIsNull(name);
    }

    public List<Specialization> findAllSpecialization() {
        return specializationRepository.findAllByDeletedAtIsNull();
    }

    public List<Specialization> findAllSpecializationById(List<String> specializationIds) {
        return specializationRepository.findAllByIdInAndDeletedAtIsNull(specializationIds);
    }

    public Optional<Specialization> findSpecializationByName(String name) {
        return specializationRepository.findByNameAndDeletedAtIsNull(name);
    }

    public void deleteSpecialization(Specialization specialization) {
        specialization.setDeletedAt(LocalDateTime.now());
        Specialization deletedSpecialization = specializationRepository.save(specialization);
        cleanSpecializationRepos(deletedSpecialization);
    }

    public Specialization createSpecialization(com.jadwal.restfulapi.dto.SpecializationDTO specializationDTO, User user) {
        Specialization specialization = new Specialization();
        specialization.setName(specializationDTO.getName());
        specialization.setCreatedBy(user);
        specialization.setEditedBy(user);
        specialization.setCreatedAt(LocalDateTime.now());
        specialization.setUpdatedAt(LocalDateTime.now());
        return specializationRepository.save(specialization);
    }

    public Specialization editSpecialization(Specialization editedSpecialization, SpecializationDTO specializationDTO,
            User user) {
        editedSpecialization.setName(specializationDTO.getName());
        editedSpecialization.setEditedBy(user);
        editedSpecialization.setUpdatedAt(LocalDateTime.now());
        return specializationRepository.save(editedSpecialization);
    }

    public List<String> checkNonExistentSpecializations(List<String> specializationIds) {
        List<Specialization> existingSpecializations = findAllSpecializationById(specializationIds);
        specializationIds.removeIf(id -> existingSpecializations.stream().anyMatch(s -> s.getId().equals(id)));
        return specializationIds;
    }

    @Transactional
    public void cleanSpecializationRepos(Specialization specialization) {
        lecturerSpecializationRepository.deleteAllBySpecializationId(specialization);
        labSpecializationRepository.deleteAllBySpecializationId(specialization);
        courseSpecializationRepository.deleteAllBySpecializationId(specialization);
    }
}