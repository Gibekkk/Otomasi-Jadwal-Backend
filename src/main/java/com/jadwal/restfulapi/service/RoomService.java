package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.dto.RoomDTO;
import com.jadwal.restfulapi.model.Room;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.repository.RoomRepository;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public Optional<Room> findRoomById(String id) {
        return roomRepository.findByIdAndDeletedAtIsNull(id);
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

    public void deleteRoom(Room room) {
        room.setDeletedAt(LocalDateTime.now());
        roomRepository.save(room);
    }

    public Room createRoom(RoomDTO roomDTO, User user) {
        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setCapacity(roomDTO.getCapacity());
        room.setCreatedBy(user);
        room.setEditedBy(user);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepository.save(room);
    }

    public Room editRoom(Room editedRoom, RoomDTO roomDTO,
            User user) {
        editedRoom.setName(roomDTO.getName());
        editedRoom.setCapacity(roomDTO.getCapacity());
        editedRoom.setEditedBy(user);
        editedRoom.setUpdatedAt(LocalDateTime.now());
        return roomRepository.save(editedRoom);
    }

    public List<String> checkNonExistentRooms(List<String> roomIds) {
        List<Room> existingRooms = findAllRoomById(roomIds);
        roomIds.removeIf(id -> existingRooms.stream().anyMatch(s -> s.getId().equals(id)));
        return roomIds;
    }
}