package com.jadwal.restfulapi.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CategoryDTOTest {

    @Test
    void checkDTOPassesAndTrimsAValidName() {
        CategoryDTO dto = new CategoryDTO("  Wajib  ", null);

        dto.checkDTO();

        assertThat(dto.getName()).isEqualTo("Wajib");
    }

    @Test
    void checkDTOThrowsWhenNameIsNull() {
        CategoryDTO dto = new CategoryDTO(null, null);

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name Cannot Be NULL");
    }

    @Test
    void checkDTOThrowsWhenNameIsOnlyWhitespace() {
        CategoryDTO dto = new CategoryDTO("     ", null);

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name Cannot Be NULL");
    }

    @Test
    void checkDTOThrowsWhenNameExceedsMaxLength() {
        CategoryDTO dto = new CategoryDTO("a".repeat(51), null);

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name Exceeded Max Length");
    }

    @Test
    void checkDTOAcceptsNameAtMaxLength() {
        CategoryDTO dto = new CategoryDTO("a".repeat(50), null);

        dto.checkDTO();

        assertThat(dto.getName()).hasSize(50);
    }
}
