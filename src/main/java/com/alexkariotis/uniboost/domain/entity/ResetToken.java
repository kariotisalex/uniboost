package com.alexkariotis.uniboost.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "RESET_TOKEN")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetToken {

    @Id
    private UUID id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expired_at",nullable = false)
    private Date expiredAt;

    @Column(name = "created_at")
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "used")
    private boolean used;

    public static ResetToken newToken(User user) {
        long thirtyMinutes = 30 * 60 * 1000;
        return ResetToken
                .builder()
                .id(UUID.randomUUID())
                .token(token())
                .expiredAt(new Date(System.currentTimeMillis() + thirtyMinutes))
                .createdAt(new Date(System.currentTimeMillis()))
                .user(user)
                .used(false)
                .build();
    }

    private static String token(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}