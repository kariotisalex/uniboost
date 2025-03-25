package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.api.filter.utils.JwtUtils;
import com.alexkariotis.uniboost.common.TokenTypeEnum;
import com.alexkariotis.uniboost.domain.entity.Token;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.TokenRepository;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import com.alexkariotis.uniboost.dto.user.AuthenticationRequestDto;
import com.alexkariotis.uniboost.dto.user.AuthenticationResponseDto;
import io.vavr.Tuple;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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
        log.info("AuthenticationService.register(User user)");
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
                                .map(jwtUtils::generateToken)
                                .map(token -> {
                                    Token tokenObj = Token.builder()
                                            .id(UUID.randomUUID())
                                            .user(user)
                                            .token(token)
                                            .tokenTypeEnum(TokenTypeEnum.BEARER)
                                            .revoked(false)
                                            .expired(false)
                                            .build();
                                    return tokenRepository.save(tokenObj);
                                })
                                .map(tokenObj -> new AuthenticationResponseDto(tokenObj.getToken()))
                        ,ignoredUserByEmail -> Try.failure(new IllegalArgumentException("Email already exists")))
                ,ignoredUserByUsername -> Try.failure(new IllegalArgumentException("Username already exists")));
    }

    public Try<AuthenticationResponseDto> authenticate(AuthenticationRequestDto requestDto) {
        log.info("AuthenticationService.authenticate(AuthenticationRequestDto requestDto)");
        return Try.of(() -> Option.ofOptional(userRepository.findByUsername(requestDto.getUsername()))
                        .getOrElseThrow(() -> new IllegalArgumentException("There is no user with this username")))
                .flatMap(user -> Try.of(() -> authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        requestDto.getUsername(),
                                        requestDto.getPassword())))
                        .map(authentication -> user)
                        .recover(BadCredentialsException.class, e -> {
                            throw new IllegalArgumentException("Wrong password provided", e);
                        }))
                .map(user -> Tuple.of(user, jwtUtils.generateToken(user)))
                .flatMap(tuple -> {
                    User userObj = tuple._1;
                    String jwt = tuple._2;
                    return Try.of(()-> tokenRepository.findAllValidTokensByUser(userObj.getId()))
                            .map(tokens -> tokens
                                    .stream()
                                    .map(token -> {
                                        token.setExpired(true);
                                        token.setRevoked(true);
                                        return token;
                                    }).toList())
                            .map(tokenRepository::saveAll)
                            .map(ignored -> tuple);
                })
                .map(tuple -> {
                    Token tokenObj = Token.builder()
                            .id(UUID.randomUUID())
                            .user(tuple._1)
                            .token(tuple._2)
                            .tokenTypeEnum(TokenTypeEnum.BEARER)
                            .revoked(false)
                            .expired(false)
                            .build();
                    return tokenRepository.save(tokenObj);
                })
                .map(tokenObj -> new AuthenticationResponseDto(tokenObj.getToken()));
    }


}
