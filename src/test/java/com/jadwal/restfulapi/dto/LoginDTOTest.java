package com.jadwal.restfulapi.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LoginDTOTest {

    @Test
    void checkDTOPassesWithValidUsernameAndPassword() {
        LoginDTO dto = new LoginDTO("validuser", "secret");

        dto.checkDTO();

        assertThat(dto.getUsername()).isEqualTo("validuser");
        assertThat(dto.getPassword()).isEqualTo("secret");
    }

    @Test
    void checkDTOTrimsWhitespaceFromBothFields() {
        LoginDTO dto = new LoginDTO("  validuser  ", "  secret  ");

        dto.checkDTO();

        assertThat(dto.getUsername()).isEqualTo("validuser");
        assertThat(dto.getPassword()).isEqualTo("secret");
    }

    @Test
    void checkDTOThrowsWhenUsernameIsNull() {
        LoginDTO dto = new LoginDTO(null, "secret");

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username Cannot Be NULL");
    }

    @Test
    void checkDTOThrowsWhenUsernameIsBlank() {
        LoginDTO dto = new LoginDTO("   ", "secret");

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username Cannot Be NULL");
    }

    @Test
    void checkDTOThrowsWhenPasswordIsNull() {
        LoginDTO dto = new LoginDTO("validuser", null);

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password Cannot Be NULL");
    }

    @Test
    void checkDTOThrowsWhenUsernameTooShort() {
        LoginDTO dto = new LoginDTO("ab", "secret");

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username Must Be Between 3 To 15 Characters");
    }

    @Test
    void checkDTOThrowsWhenUsernameTooLong() {
        LoginDTO dto = new LoginDTO("a".repeat(16), "secret");

        assertThatThrownBy(dto::checkDTO)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username Must Be Between 3 To 15 Characters");
    }

    @Test
    void checkDTOAcceptsUsernameAtBoundaryLengths() {
        LoginDTO shortest = new LoginDTO("abc", "secret");
        LoginDTO longest = new LoginDTO("a".repeat(15), "secret");

        shortest.checkDTO();
        longest.checkDTO();

        assertThat(shortest.getUsername()).hasSize(3);
        assertThat(longest.getUsername()).hasSize(15);
    }
}
