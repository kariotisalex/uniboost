package com.alexkariotis.uniboost.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@RequiredArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;

}
