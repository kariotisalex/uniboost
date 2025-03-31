package com.alexkariotis.uniboost.api.controller;

import com.alexkariotis.uniboost.common.Constants;
import com.alexkariotis.uniboost.dto.user.AuthenticationRequestDto;
import com.alexkariotis.uniboost.dto.user.AuthenticationResponseDto;
import com.alexkariotis.uniboost.dto.user.UserCreateDto;
import com.alexkariotis.uniboost.mapper.user.UserMapper;
import com.alexkariotis.uniboost.service.UserService;
import io.vavr.control.Try;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(Constants.USER)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("register")
    public ResponseEntity<AuthenticationResponseDto> register(
            @RequestBody UserCreateDto createDto
    ) {
        return userService
                .register(UserMapper.userCreateDtoToUser(createDto))
                .map(ResponseEntity::ok)
                .onFailure(Throwable::printStackTrace)
                .recoverWith(ex ->
                        Try.success(ResponseEntity
                                .badRequest()
                                .body(new AuthenticationResponseDto()))
                )
                .getOrElseThrow(ex ->
                        new RuntimeException("User didn't create. Please report the problem to admin.", ex));
    }

    @PostMapping("login")
    public ResponseEntity<AuthenticationResponseDto> login(
            @RequestBody AuthenticationRequestDto authenticationRequestDto
    ) {

        return userService
                .authenticate(authenticationRequestDto)
                .map(ResponseEntity::ok)
                .onFailure(Throwable::printStackTrace)
                .getOrElseThrow(ex ->
                        new RuntimeException("User didn't create. Please report the problem to admin.", ex));
    }

    @PostMapping("refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
         userService.refreshToken(request, response);
    }
}
