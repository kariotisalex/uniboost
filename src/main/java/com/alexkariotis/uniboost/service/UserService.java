package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.api.filter.utils.JwtUtils;
import com.alexkariotis.uniboost.common.TokenTypeEnum;
import com.alexkariotis.uniboost.domain.entity.Token;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.TokenRepository;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import com.alexkariotis.uniboost.dto.user.AuthenticationRequestDto;
import com.alexkariotis.uniboost.dto.user.AuthenticationResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;


    public Try<AuthenticationResponseDto> register(User user) {
        log.info("UserService.register(User user)");
        return Option.ofOptional(userRepository.findByUsername(user.getUsername()))
                .fold(() -> Option.ofOptional(userRepository.findByEmail(user.getEmail()))
                        .fold(() -> Try.of(() -> user)
                                .map(u -> {
                                    u.setId(UUID.randomUUID());
                                    u.setPassword(passwordEncoder.encode(u.getPassword()));
                                    u.setCreatedAt(OffsetDateTime.now());
                                    u.setUpdatedAt(OffsetDateTime.now());
                                    return u;
                                })
                                .map(userRepository::save)
                                .map(fetchedUser -> Tuple.of(fetchedUser,
                                        jwtUtils.generateToken(fetchedUser),
                                        jwtUtils.generateRefreshToken(fetchedUser)))
                                .map(tuple -> Tuple.of(saveAccessToken(tuple._1, tuple._2)
                                        .getOrElseThrow(ex ->
                                                new RuntimeException("Access Tokens haven't saved in databased!", ex))
                                        ,tuple._3))
                                .map(tuple -> AuthenticationResponseDto
                                        .builder()
                                        .accessToken(tuple._1.getToken())
                                        .refreshToken(tuple._2)
                                        .build())
                        ,ignoredUserByEmail -> Try.failure(new IllegalArgumentException("Email already exists")))
                ,ignoredUserByUsername -> Try.failure(new IllegalArgumentException("Username already exists")));
    }

    public Try<AuthenticationResponseDto> authenticate(AuthenticationRequestDto requestDto) {
        log.info("UserService.authenticate(AuthenticationRequestDto requestDto)");
        return Try.of(() -> Option.ofOptional(userRepository.findByUsername(requestDto.getUsername()))
                        .getOrElseThrow(() -> new UsernameNotFoundException("There is no user with username: "+requestDto.getUsername())))
                .flatMap(user -> Try.of(() -> authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        requestDto.getUsername(),
                                        requestDto.getPassword())))
                        .map(authentication -> user)
                        .recover(BadCredentialsException.class, e -> {
                            throw new IllegalArgumentException("Wrong password provided", e);
                        }))
                .map(user -> Tuple.of(
                        user,
                        jwtUtils.generateToken(user),
                        jwtUtils.generateRefreshToken(user)))
                .flatMap(tuple -> revokeAllAccessTokens(tuple._1)
                            .map(ignored -> tuple))
                .map(tuple -> Tuple.of(saveAccessToken(tuple._1, tuple._2)
                        .getOrElseThrow(ex -> new RuntimeException("Access Token didn't save in database.", ex))
                        ,tuple._3))
                .map(tuple -> AuthenticationResponseDto
                        .builder()
                        .accessToken(tuple._1.getToken())
                        .refreshToken(tuple._2)
                        .build());
    }




    public void refreshToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String username;

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        username = jwtUtils.extractUsername(refreshToken);

        if(username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username: "+username+" not found"));
            if(jwtUtils.isTokenValid(refreshToken, user) ) {
                var accessToken = jwtUtils.generateToken(user);
                revokeAllAccessTokens(user);
                saveAccessToken(user,accessToken);
                var authResponse = AuthenticationResponseDto
                        .builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();


                new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
            }
        }
    }



    private Try<Token> saveAccessToken(User user, String token) {
        return Try.of(() -> tokenRepository
                .save(Token.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .token(token)
                        .tokenTypeEnum(TokenTypeEnum.BEARER)
                        .revoked(false)
                        .expired(false)
                        .build()));
    }

    private Try<List<Token>> revokeAllAccessTokens(User user) {
        return Try.of(()-> tokenRepository.findAllValidTokensByUser(user.getId()))
                .map(tokens -> tokens
                        .stream()
                        .map(token -> {
                            token.setExpired(true);
                            token.setRevoked(true);
                            return token;
                        }).toList())
                .map(tokenRepository::saveAllAndFlush);
    }
}
