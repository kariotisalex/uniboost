package com.alexkariotis.uniboost.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoRequestDto {

    private String firstname;
    private String lastname;
    private String phone;
}
