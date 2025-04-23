package com.alexkariotis.uniboost.domain.entity;

import com.alexkariotis.uniboost.common.JwtTokenTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Table(name = "JWT_TOKEN")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class JwtToken {

    @Id
    private UUID id;

    @Column(name = "token", nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "token_type_enum", nullable = false)
    private JwtTokenTypeEnum jwtTokenTypeEnum;

    @Column(name = "expired", nullable = false)
    private boolean expired;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


}
