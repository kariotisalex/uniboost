package com.alexkariotis.uniboost.dto.user;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

}
