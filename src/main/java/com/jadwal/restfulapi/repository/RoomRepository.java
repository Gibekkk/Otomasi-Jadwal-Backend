package com.jadwal.restfulapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jadwal.restfulapi.model.Room;

public interface RoomRepository extends JpaRepository<Room, String> {
    public Optional<Room> findByIdAndDeletedAtIsNull(String id);
    public List<Room> findAllByDeletedAtIsNull();
}
