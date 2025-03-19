package com.alexkariotis.uniboost.dto.user;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequestDto {

    private String username;
    private String password;
}
