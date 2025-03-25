package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.domain.entity.Token;
import com.alexkariotis.uniboost.domain.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(fetchedToken -> !fetchedToken.isExpired() && !fetchedToken.isRevoked())
                .orElse(false);
    }

}
