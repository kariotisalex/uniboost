package com.alexkariotis.uniboost.domain.repository;

import com.alexkariotis.uniboost.domain.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository  extends JpaRepository<Token, UUID> {


    @Query("""
        SELECT t
        FROM Token t
        inner join User u on t.user.id = u.id
        WHERE u.id = :userId and (t.expired = false or t.revoked = false)
    """)
    List<Token> findAllValidTokensByUser(@Param("userId") UUID userId);


    Optional<Token> findByToken(String token);
}
