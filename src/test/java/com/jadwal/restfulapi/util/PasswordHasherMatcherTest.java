package com.jadwal.restfulapi.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHasherMatcherTest {

    private final PasswordHasherMatcher passwordHasherMatcher = new PasswordHasherMatcher();

    @Test
    void hashPasswordProducesABCryptHashDifferentFromTheRawInput() {
        String hash = passwordHasherMatcher.hashPassword("my-secret-password");

        assertThat(hash).isNotEqualTo("my-secret-password");
        assertThat(hash).startsWith("$2a$");
    }

    @Test
    void hashingTheSamePasswordTwiceProducesDifferentSaltedHashes() {
        String firstHash = passwordHasherMatcher.hashPassword("my-secret-password");
        String secondHash = passwordHasherMatcher.hashPassword("my-secret-password");

        assertThat(firstHash).isNotEqualTo(secondHash);
    }

    @Test
    void matchPasswordReturnsTrueForTheCorrectRawPassword() {
        String hash = passwordHasherMatcher.hashPassword("correct-password");

        assertThat(passwordHasherMatcher.matchPassword("correct-password", hash)).isTrue();
    }

    @Test
    void matchPasswordReturnsFalseForAnIncorrectRawPassword() {
        String hash = passwordHasherMatcher.hashPassword("correct-password");

        assertThat(passwordHasherMatcher.matchPassword("wrong-password", hash)).isFalse();
    }
}
