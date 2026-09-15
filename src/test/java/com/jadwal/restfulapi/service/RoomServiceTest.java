package com.jadwal.restfulapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jadwal.restfulapi.dto.LabGroupDTO;
import com.jadwal.restfulapi.dto.RoomDTO;
import com.jadwal.restfulapi.model.LabGroup;
import com.jadwal.restfulapi.model.Room;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.repository.LabGroupRepository;
import com.jadwal.restfulapi.repository.LabSpecializationRepository;
import com.jadwal.restfulapi.repository.RoomRepository;

/**
 * Unit tests for {@link RoomService}.
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private LabGroupRepository labGroupRepository;

    @Mock
    private LabSpecializationRepository labSpecializationRepository;

    @InjectMocks
    private RoomService roomService;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId("admin-id");
    }

    @Test
    @DisplayName("createRoom without a lab group leaves labGroupId null")
    void createRoomWithoutLabGroup() {
        RoomDTO dto = new RoomDTO();
        dto.setName("R101");
        dto.setCapacity(40);
        dto.setLabGroupId(null);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.createRoom(dto, admin);

        assertThat(result.getName()).isEqualTo("R101");
        assertThat(result.getCapacity()).isEqualTo(40);
        assertThat(result.getLabGroupId()).isNull();
        verify(labGroupRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("createRoom with a lab group id attaches the resolved lab group")
    void createRoomWithLabGroup() {
        LabGroup labGroup = new LabGroup();
        labGroup.setId("lab-1");
        labGroup.setName("Jaringan");

        RoomDTO dto = new RoomDTO();
        dto.setName("Lab 1");
        dto.setCapacity(30);
        dto.setLabGroupId("lab-1");

        when(labGroupRepository.findByIdAndDeletedAtIsNull("lab-1")).thenReturn(Optional.of(labGroup));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.createRoom(dto, admin);

        assertThat(result.getLabGroupId()).isEqualTo(labGroup);
    }

    @Test
    @DisplayName("editRoom clears the lab group when the DTO no longer references one")
    void editRoomClearsLabGroupWhenNullInDto() {
        LabGroup previousLabGroup = new LabGroup();
        previousLabGroup.setId("lab-1");

        Room existing = new Room();
        existing.setId("room-1");
        existing.setLabGroupId(previousLabGroup);

        RoomDTO dto = new RoomDTO();
        dto.setName("R101 Renamed");
        dto.setCapacity(50);
        dto.setLabGroupId(null);

        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.editRoom(existing, dto, admin);

        assertThat(result.getName()).isEqualTo("R101 Renamed");
        assertThat(result.getCapacity()).isEqualTo(50);
        assertThat(result.getLabGroupId()).isNull();
        assertThat(result.getEditedBy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("deleteRoom soft deletes without touching related lab groups")
    void deleteRoomSoftDeletes() {
        Room room = new Room();
        room.setId("room-1");
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roomService.deleteRoom(room);

        assertThat(room.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteLabGroup soft deletes the lab group and detaches every room in it")
    void deleteLabGroupDetachesRooms() {
        LabGroup labGroup = new LabGroup();
        labGroup.setId("lab-1");

        Room roomOne = new Room();
        roomOne.setLabGroupId(labGroup);
        Room roomTwo = new Room();
        roomTwo.setLabGroupId(labGroup);
        Set<Room> rooms = new HashSet<>();
        rooms.add(roomOne);
        rooms.add(roomTwo);
        labGroup.setLabRooms(rooms);

        when(labGroupRepository.save(any(LabGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        when(roomRepository.save(roomCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        roomService.deleteLabGroup(labGroup);

        assertThat(labGroup.getDeletedAt()).isNotNull();
        assertThat(roomCaptor.getAllValues()).hasSize(2);
        roomCaptor.getAllValues().forEach(room -> assertThat(room.getLabGroupId()).isNull());
    }

    @Test
    @DisplayName("createLabGroup persists one LabSpecialization per given specialization")
    void createLabGroupPersistsSpecializations() {
        LabGroupDTO dto = new LabGroupDTO();
        dto.setName("Jaringan Komputer");

        Specialization specOne = new Specialization();
        specOne.setId("spec-1");
        Specialization specTwo = new Specialization();
        specTwo.setId("spec-2");

        when(labGroupRepository.save(any(LabGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LabGroup result = roomService.createLabGroup(dto, admin, List.of(specOne, specTwo));

        assertThat(result.getName()).isEqualTo("Jaringan Komputer");
        verify(labSpecializationRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("editLabGroup clears old specializations before re-attaching the new ones")
    void editLabGroupReplacesSpecializations() {
        LabGroup existing = new LabGroup();
        existing.setId("lab-1");
        existing.setName("Old Name");

        LabGroupDTO dto = new LabGroupDTO();
        dto.setName("New Name");

        Specialization spec = new Specialization();
        spec.setId("spec-1");

        when(labGroupRepository.save(any(LabGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LabGroup result = roomService.editLabGroup(existing, dto, admin, List.of(spec));

        assertThat(result.getName()).isEqualTo("New Name");
        verify(labSpecializationRepository, times(1)).deleteAllByLabGroupId(existing);
        verify(labSpecializationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("isRoomExistByName and isRoomExistByNameAndIdIsNot delegate to the repository")
    void existsByNameHelpersDelegate() {
        when(roomRepository.existsByNameAndDeletedAtIsNull("R101")).thenReturn(true);
        when(roomRepository.existsByNameAndDeletedAtIsNullAndIdIsNot("R101", "room-1")).thenReturn(false);

        assertThat(roomService.isRoomExistByName("R101")).isTrue();
        assertThat(roomService.isRoomExistByNameAndIdIsNot("R101", "room-1")).isFalse();
    }
}
