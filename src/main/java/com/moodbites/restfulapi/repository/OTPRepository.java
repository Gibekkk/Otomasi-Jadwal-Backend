package com.moodbites.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.moodbites.restfulapi.model.OTP;
import com.moodbites.restfulapi.model.User;

public interface OTPRepository extends JpaRepository<OTP, String> {
    Optional<OTP> findByUserId(User userId);
}
