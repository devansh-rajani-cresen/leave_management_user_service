package com.cresensolutions.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "otp", schema = "otp")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email_id", unique = true)
    private String emailId;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
}
