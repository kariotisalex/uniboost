package com.alexkariotis.uniboost.domain.repository;

import com.alexkariotis.uniboost.domain.entity.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {


}
