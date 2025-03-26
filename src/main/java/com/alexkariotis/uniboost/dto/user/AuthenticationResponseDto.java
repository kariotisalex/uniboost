package com.alexkariotis.uniboost.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponseDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_Token")
    private String refreshToken;
}
