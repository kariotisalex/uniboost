package com.alexkariotis.uniboost.domain.repository;

import com.alexkariotis.uniboost.domain.entity.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {


    @Query("SELECT rt FROM ResetToken rt WHERE rt.token=:token")
    Optional<ResetToken> findByToken(@Param("token") String token);
}
