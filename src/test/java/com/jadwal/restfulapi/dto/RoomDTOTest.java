package com.jadwal.restfulapi.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RoomDTOTest {

    @Test
    void checkDTOPassesWithValidNameAndCapacity() {
        RoomDTO dto = new RoomDTO("  Room 101  ", "lab-1", 30);

        dto.checkDTO();

        assertThat(dto.getName()).isEqualTo("Room 101");
        assertThat(dto.getCapacity()).isEqualTo(30);
    }

    @Test
    void checkDTOThrowsWhenNameIsNull() {
        RoomDTO dto = new RoomDTO(null, null, 30);

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name Cannot Be NULL");
    }

    @Test
    void checkDTOThrowsWhenNameExceedsMaxLength() {
        RoomDTO dto = new RoomDTO("a".repeat(51), null, 30);

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name Exceeded Max Length");
    }

    @Test
    void checkDTOThrowsWhenCapacityBelowMinimum() {
        RoomDTO dto = new RoomDTO("Room 101", null, 4);

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Capacity Must Be At Least 5");
    }

    @Test
    void checkDTOAcceptsCapacityAtMinimumBoundary() {
        RoomDTO dto = new RoomDTO("Room 101", null, 5);

        dto.checkDTO();

        assertThat(dto.getCapacity()).isEqualTo(5);
    }

    @Test
    void trimBlanksOutAWhitespaceOnlyLabGroupId() {
        RoomDTO dto = new RoomDTO("Room 101", "   ", 10);

        dto.trim();

        assertThat(dto.getLabGroupId()).isNull();
    }
}
