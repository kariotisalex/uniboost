package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.api.filter.utils.JwtUtils;
import com.alexkariotis.uniboost.common.JwtTokenTypeEnum;
import com.alexkariotis.uniboost.domain.entity.JwtToken;
import com.alexkariotis.uniboost.domain.entity.ResetToken;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.ResetTokenRepository;
import com.alexkariotis.uniboost.domain.repository.TokenRepository;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import com.alexkariotis.uniboost.dto.SendEmailDto;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Setter
@Getter
@RequiredArgsConstructor
public class UserService {

    @Value("${link.reset-password}")
    private String resetLink = "";

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
                                .map(userRepository::saveAndFlush)
                                        .map(user1 -> {
                                            emailService.sendEmail(SendEmailDto.builder()
                                                    .sendTo(user1.getEmail())
                                                    .subject("""
                                                            Welcome to Uniboost – Your journey starts here
                                                            """)
                                                    .body("""
                                                            Dear %s,
                                                                                                                        
                                                            Welcome to Uniboost! \s
                                                            We're excited to have you as part of our academic community.
                                                                                                                        
                                                            With your new profile, you can now:
                                                            • Enroll in courses \s
                                                            • Track your progress \s
                                                            • Connect with instructors \s
                                                            • And access everything you need from your personal dashboard
                                                                                                                        
                                                            We recommend logging in and completing your profile to get the best experience.
                                                                                                                        
                                                            If you have any questions or need support, we’re here to help through the platform.
                                                                                                                        
                                                            This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                                                                                                        
                                                            —
                                                            Uniboost Academic Services
                                                            """.formatted(user1.getFirstname()))
                                                    .build());
                                            return user1;
                                        })
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
                        .getOrElseThrow(() -> new IllegalArgumentException("There is no user with username: "+requestDto.getUsername())))
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
                    .orElseThrow(() -> new IllegalArgumentException("Username: "+username+" not found"));
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
                }).map(resetToken -> {
                    emailService.sendEmail(SendEmailDto.builder()
                        .sendTo(resetToken.getUser().getEmail())
                        .subject("Your Uniboost Password Has Been Successfully Reset")
                        .body("""
                            Dear %s,
                            
                            We’re confirming that your password for your Uniboost account was successfully reset using a secure verification token.
                            
                            If you made this change, no further action is required.
                            
                            If you did **not** request this reset, we strongly recommend changing your password immediately and contacting support through the platform.
                            
                            This is an automated message. Please do not reply to this email.
                            
                            —
                            Uniboost Security Services
                            """.formatted(resetToken.getUser().getFirstname()))
                        .build());
                    return null;
                });
    }

    public Try<Void> requestToken(String username) {
        return Try.of(() -> userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username not found")))
                .map(user -> resetTokenRepository.saveAndFlush(ResetToken.newToken(user)))
                .flatMap(resetToken -> emailService.sendEmail(SendEmailDto.builder()
                                .sendTo(resetToken.getUser().getEmail())
                                .subject("Reset Your Password – Uniboost")
                                .body("""
            Hello %s,

            We received a request to reset your password for your Uniboost account.

            Click the link below to choose a new password:
            %s

            This link will expire in 30 minutes. If you didn't request a password reset, you can safely ignore this email.

            Thanks,
            The Uniboost Team
            """.formatted(resetToken.getUser().getFirstname(), resetLink+resetToken.getToken()))
                        .build()));

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
                        .getOrElseThrow(() -> new IllegalArgumentException("There is no user with username : "+username)))
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


    public Try<Void> delete(String username) {
        return Try.of(() -> Option.ofOptional(userRepository.findByUsername(username))
                        .getOrElseThrow(() -> new IllegalArgumentException("There is no user with this username: "+username))
                ).flatMap(user -> Try.run(() -> userRepository.delete(user))
                .onFailure(Throwable::printStackTrace)
                .flatMap(ignored -> emailService.sendEmail(SendEmailDto.builder()
                        .sendTo(user.getEmail())
                        .subject("""
                                Account Deletion Confirmation – Your Uniboost profile has been removed
                                """)
                        .body("""
                                Dear %s,
                                
                                This message is to confirm that your Uniboost profile has been successfully deleted.
                                
                                All associated course enrollments, personal information, and activity history have been permanently removed from our system in accordance with our data handling policies.
                                
                                If this action was performed in error, please note that account recovery is not possible. You are welcome to re-register at any time by creating a new profile.
                                
                                This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                
                                —
                                Uniboost Academic Services
                                """.formatted(user.getFirstname())).build())));
    }
}
