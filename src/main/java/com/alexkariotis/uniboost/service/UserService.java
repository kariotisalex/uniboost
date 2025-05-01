package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.api.filter.utils.JwtUtils;
import com.alexkariotis.uniboost.common.JwtTokenTypeEnum;
import com.alexkariotis.uniboost.domain.entity.JwtToken;
import com.alexkariotis.uniboost.domain.entity.ResetToken;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.ResetTokenRepository;
import com.alexkariotis.uniboost.domain.repository.TokenRepository;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import com.alexkariotis.uniboost.dto.user.AuthenticationRequestDto;
import com.alexkariotis.uniboost.dto.user.AuthenticationResponseDto;
import com.alexkariotis.uniboost.dto.user.UserInfoRequestDto;
import com.alexkariotis.uniboost.dto.user.UserPostResponseDto;
import com.alexkariotis.uniboost.mapper.user.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
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
    private final ResetTokenRepository resetTokenRepository;
    private final EmailService emailService;


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


    /**
     * Just refresh the access_token keeps the refresh_token the same
     * @param request .
     * @param response .
     * @throws IOException .
     */
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




    private Try<JwtToken> saveAccessToken(User user, String token) {
        return Try.of(() -> tokenRepository
                .save(JwtToken.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .token(token)
                        .jwtTokenTypeEnum(JwtTokenTypeEnum.BEARER)
                        .revoked(false)
                        .expired(false)
                        .build()));
    }

    private Try<List<JwtToken>> revokeAllAccessTokens(User user) {
        return Try.of(()-> tokenRepository.findAllValidTokensByUser(user.getId()))
                .map(tokens -> tokens
                        .stream()
                        .map(jwtToken -> {
                            jwtToken.setExpired(true);
                            jwtToken.setRevoked(true);
                            return jwtToken;
                        }).toList())
                .map(tokenRepository::saveAllAndFlush);
    }

    public Try<Void> resetPassword(String token, String password) {
        return Try.of(() -> resetTokenRepository.findByToken(token)
                .orElseThrow(()-> new RuntimeException("Invalid Token!")))
                .filter(resetToken -> !resetToken.isUsed() &&
                                resetToken.getExpiredAt().after(new Date(System.currentTimeMillis())),
                        ex -> new RuntimeException("This token is used!"))
                .map(resetToken -> {
                    resetToken.getUser().setPassword(passwordEncoder.encode(password));
                    resetToken.setUsed(true);
                    return resetTokenRepository.saveAndFlush(resetToken);
                }).map(r -> null);
    }

    public Try<Void> requestToken(String username) {
        return Try.of(() -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found")))
                .map(user -> resetTokenRepository.saveAndFlush(ResetToken.newToken(user)))
                .flatMap(resetToken -> emailService.initiatePasswordReset(resetToken.getUser(),resetToken.getToken()));

    }



    public Try<UserPostResponseDto> updateUserInfo(String username, UserInfoRequestDto userInfoRequestDto) {
        return Try.of(()-> Option.ofOptional(userRepository.findByUsername(username))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no user with username"+username)))
                .map(user -> {
                    user.setFirstname(userInfoRequestDto.getFirstname());
                    user.setLastname(userInfoRequestDto.getLastname());
                    user.setPhone(userInfoRequestDto.getPhone());
                    return userRepository.saveAndFlush(user);
                }).map(UserMapper::usertoUserPostResponseDto);
    }

    public Try<UserPostResponseDto> updateUsername(String oldUsername, String newUsername) {
        // Wrap the lookup of the user with the old username in a Try.
        return Try.of(() ->
                        // Try to find the user by oldUsername (returns Optional).
                        // If the user is not found, throw an IllegalArgumentException.
                        Option.ofOptional(userRepository.findByUsername(oldUsername))
                                .getOrElseThrow(() -> new IllegalArgumentException("There is no user with this username: " + oldUsername))
                )
                // flatMap allows us to continue processing if the Try above was successful.
                .flatMap(user ->
                        // Try to find if the new username already exists.
                        Option.ofOptional(userRepository.findByUsername(newUsername))
                                // If a user with the new username exists, return a failed Try with an exception.
                                .map(existingUser -> Try.<UserPostResponseDto>failure(new IllegalArgumentException("Username already exists!")))
                                // If new username is available (Optional is empty), update the username and save.
                                .getOrElse(() -> {
                                    // Set the new username on the found user
                                    user.setUsername(newUsername);
                                    user.setUpdatedAt(OffsetDateTime.now());
                                    // Save the updated user and map the result to a DTO inside a Try
                                    return Try.of(() -> userRepository.saveAndFlush(user))
                                            .map(UserMapper::usertoUserPostResponseDto);
                                })
                );
    }
    public Try<UserPostResponseDto> findByUsername(String username) {
        return Try.of(() -> Option.ofOptional(userRepository.findByUsername(username))
                        .getOrElseThrow(() -> new UsernameNotFoundException("There is no user with username : "+username)))
                .map(UserMapper::usertoUserPostResponseDto);
    }



    public Try<UserPostResponseDto> updateEmail(String username, String email) {
        return Try.of(() ->Option.ofOptional(userRepository.findByUsername(username))
                                .getOrElseThrow(() -> new IllegalArgumentException("There is no user with this username: " + username)))
                .flatMap(user ->
                        Option.ofOptional(userRepository.findByEmail(email))
                                .map(existingUser -> Try.<UserPostResponseDto>failure(new IllegalArgumentException("Username already exists!")))
                                .getOrElse(() -> {
                                    user.setEmail(email);
                                    user.setUpdatedAt(OffsetDateTime.now());
                                    return Try.of(() -> userRepository.saveAndFlush(user))
                                            .map(UserMapper::usertoUserPostResponseDto);
                                })
                                .onFailure(Throwable::printStackTrace)
                );

    }



}
