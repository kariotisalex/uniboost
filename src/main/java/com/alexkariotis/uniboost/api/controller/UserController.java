package com.alexkariotis.uniboost.api.controller;

import com.alexkariotis.uniboost.api.filter.utils.JwtUtils;
import com.alexkariotis.uniboost.common.Constants;
import com.alexkariotis.uniboost.dto.post.ResetPasswordRequestDto;
import com.alexkariotis.uniboost.dto.user.*;
import com.alexkariotis.uniboost.mapper.user.UserMapper;
import com.alexkariotis.uniboost.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(Constants.USER)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {


    private final UserService userService;
    private final JwtUtils jwtUtils;





    @PostMapping("register")
    public ResponseEntity<AuthenticationResponseDto> register(
            @RequestBody UserCreateDto createDto
    ) {
        return userService
                .register(UserMapper.userCreateDtoToUser(createDto))
                .map(ResponseEntity::ok)
                .onFailure(Throwable::printStackTrace)
                .get();
    }

    @PostMapping("login")
    public ResponseEntity<AuthenticationResponseDto> login(
            @RequestBody AuthenticationRequestDto authenticationRequestDto
    ) {

        return userService
                .authenticate(authenticationRequestDto)
                .map(ResponseEntity::ok)
                .onFailure(Throwable::printStackTrace)
                .get();
    }

    @PostMapping("refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        System.out.println("here");
         userService.refreshToken(request, response);
    }

    @PostMapping("forgot-password")
    public ResponseEntity<Void> requestToken(
            @RequestParam(name = "username") String username
    ) {
        return userService.requestToken(username)
                .map(ResponseEntity::ok)
                .get();
    }

    @PostMapping("reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam(name = "token") String token,
            @RequestBody ResetPasswordRequestDto requestDto
            ) {
        return userService.resetPassword(token, requestDto.getPassword())
                .map(ResponseEntity::ok)
                .get();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));
        return userService.delete(username)
                .onFailure(Throwable::printStackTrace)
                .map(ResponseEntity::ok)
                .get();
    }


// END OF AUTHORIZATION


    @GetMapping("userinfo")
    public ResponseEntity<UserPostResponseDto> myDetails(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.userInfo(");

        return this.userService.findByUsername(username)
                .onFailure(Throwable::printStackTrace)
                .map(ResponseEntity::ok)

                .get();
    }


    @PutMapping("userinfo")
    public ResponseEntity<UserPostResponseDto> updateUserInfo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody UserInfoRequestDto userPostResponseDto
    ){
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.updateUserInfo(");
        return this.userService.updateUserInfo(username,userPostResponseDto)
                .map(ResponseEntity::ok)
                .get();

    }

    @PutMapping("email")
    public ResponseEntity<UserPostResponseDto> updateEmail(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody Map<String, String> body
    ){
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.updateUserEmail()");
        String email = body.get("email");
        return userService.updateEmail(username,email)
                .onFailure(Throwable::printStackTrace)
                .map(ResponseEntity::ok)
                .get();
    }

    @PutMapping("username")
    public ResponseEntity<UserPostResponseDto> updateUsername(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody Map<String, String> body
    ){
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.updateUserUsername()");
        String newUsername = body.get("newUsername");
        return userService.updateUsername(username,newUsername)
                .map(ResponseEntity::ok)
                .get();
    }



}
