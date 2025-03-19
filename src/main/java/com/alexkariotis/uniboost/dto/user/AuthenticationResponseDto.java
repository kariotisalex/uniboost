package com.alexkariotis.uniboost.dto.user;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponseDto {
    private String token;
}
