package com.alexkariotis.uniboost.api.controller;

import com.alexkariotis.uniboost.common.Constants;
import com.alexkariotis.uniboost.dto.user.UserResponseDto;
import com.alexkariotis.uniboost.mapper.user.UserMapper;
import com.alexkariotis.uniboost.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(Constants.USER)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable("id") UUID id
    ) {
        return userService.findById(id)
                .map(UserMapper::usertoUserResponseDto)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new IllegalArgumentException("There is no user with this ID : " + id));
    }
}
