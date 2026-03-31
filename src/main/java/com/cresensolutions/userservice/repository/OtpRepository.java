package com.cresensolutions.userservice.repository;

import com.cresensolutions.userservice.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailId(String emailId);
}