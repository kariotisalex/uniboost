package com.alexkariotis.uniboost.domain.repository;

import com.alexkariotis.uniboost.domain.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository  extends JpaRepository<JwtToken, UUID> {


    @Query("""
        SELECT t
        FROM JwtToken t
        inner join User u on t.user.id = u.id
        WHERE u.id = :userId and (t.expired = false or t.revoked = false)
    """)
    List<JwtToken> findAllValidTokensByUser(@Param("userId") UUID userId);


    Optional<JwtToken> findByToken(String jwtToken);
}
