package com.alexkariotis.uniboost.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RESET_TOKEN")
public class ResetToken {

    @Id
    private UUID id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expired_at",nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "used")
    private boolean used;
}