package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.dto.LabGroupDTO;
import com.jadwal.restfulapi.dto.RoomDTO;
import com.jadwal.restfulapi.model.Room;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.LabGroup;
import com.jadwal.restfulapi.model.LabSpecialization;
import com.jadwal.restfulapi.repository.LabGroupRepository;
import com.jadwal.restfulapi.repository.LabSpecializationRepository;
import com.jadwal.restfulapi.repository.RoomRepository;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private LabGroupRepository labGroupRepository;

    @Autowired
    private LabSpecializationRepository labSpecializationRepository;

    public Optional<Room> findRoomById(String id) {
        return roomRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Boolean isRoomExistByNameAndIdIsNot(String name, String id) {
        return roomRepository.existsByNameAndDeletedAtIsNullAndIdIsNot(name, id);
    }

    public Boolean isRoomExistByName(String name) {
        return roomRepository.existsByNameAndDeletedAtIsNull(name);
    }

    public List<Room> findAllRoom() {
        return roomRepository.findAllByDeletedAtIsNull();
    }

    public List<Room> findAllRoomById(List<String> roomIds) {
        return roomRepository.findAllByIdInAndDeletedAtIsNull(roomIds);
    }

    public Optional<Room> findRoomByName(String name) {
        return roomRepository.findByNameAndDeletedAtIsNull(name);
    }

    public Optional<LabGroup> findGroupById(String id) {
        return labGroupRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Boolean isLabGroupExistById(String ig) {
        return labGroupRepository.existsByIdAndDeletedAtIsNull(ig);
    }

    public Boolean isLabGroupExistByName(String name) {
        return labGroupRepository.existsByNameAndDeletedAtIsNull(name);
    }

    public Boolean isLabGroupExistByNameAndIdIsNot(String name, String id) {
        return labGroupRepository.existsByNameAndDeletedAtIsNullAndIdIsNot(name, id);
    }

    public List<LabGroup> findAllLabGroup() {
        return labGroupRepository.findByDeletedAtIsNull();
    }

    public Optional<LabGroup> findLabGroupById(String id) {
        return labGroupRepository.findByIdAndDeletedAtIsNull(id);
    }

    public void deleteLabGroup(LabGroup labGroup) {
        labGroup.setDeletedAt(LocalDateTime.now());
        LabGroup deletedLabGroup = labGroupRepository.save(labGroup);
        for(Room room : deletedLabGroup.getLabRooms()) {
            room.setLabGroupId(null);
            roomRepository.save(room);
        }
    }

    public void deleteRoom(Room room) {
        room.setDeletedAt(LocalDateTime.now());
        roomRepository.save(room);
    }

    public LabGroup createLabGroup(LabGroupDTO labGroupDTO, User user, List<Specialization> specializations) {
        LabGroup labGroup = new LabGroup();
        labGroup.setName(labGroupDTO.getName());
        labGroup.setCreatedAt(LocalDateTime.now());
        labGroup.setUpdatedAt(LocalDateTime.now());
        labGroup.setCreatedBy(user);
        labGroup.setEditedBy(user);
        LabGroup savedLabGroup = labGroupRepository.save(labGroup);

        for (Specialization specialization : specializations) {
            labSpecializationRepository.save(new LabSpecialization(null, savedLabGroup, specialization));
        }

        return savedLabGroup;
    }

    public LabGroup editLabGroup(LabGroup editedLabGroup, LabGroupDTO labGroupDTO, User user,
            List<Specialization> specializations) {
        editedLabGroup.setName(labGroupDTO.getName());
        editedLabGroup.setUpdatedAt(LocalDateTime.now());
        editedLabGroup.setEditedBy(user);
        LabGroup savedLabGroup = labGroupRepository.save(editedLabGroup);

        deleteLabSpecializationsByLab(savedLabGroup);
        for (Specialization specialization : specializations) {
            labSpecializationRepository.save(new LabSpecialization(null, savedLabGroup, specialization));
        }

        return savedLabGroup;
    }

    public Room createRoom(RoomDTO roomDTO, User user) {
        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setCapacity(roomDTO.getCapacity());
        room.setCreatedBy(user);
        room.setEditedBy(user);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());

        if (roomDTO.getLabGroupId() != null)
            room.setLabGroupId(findGroupById(roomDTO.getLabGroupId()).get());
        else
            room.setLabGroupId(null);
        return roomRepository.save(room);
    }

    public Room editRoom(Room editedRoom, RoomDTO roomDTO,
            User user) {
        editedRoom.setName(roomDTO.getName());
        editedRoom.setCapacity(roomDTO.getCapacity());
        editedRoom.setEditedBy(user);
        editedRoom.setUpdatedAt(LocalDateTime.now());

        if (roomDTO.getLabGroupId() != null)
            editedRoom.setLabGroupId(findGroupById(roomDTO.getLabGroupId()).get());
        else
            editedRoom.setLabGroupId(null);

        return roomRepository.save(editedRoom);
    }

    public void deleteLabSpecializationsByLab(LabGroup labGroup) {
        labSpecializationRepository.deleteAllByLabGroupId(labGroup);
    }
}