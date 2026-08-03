package com.jadwal.restfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.jadwal.restfulapi.model.OTP;
import com.jadwal.restfulapi.model.User;

public interface OTPRepository extends JpaRepository<OTP, String> {
    Optional<OTP> findByUserId(User userId);
}
