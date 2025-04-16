package com.alexkariotis.uniboost.dto.user;

import com.alexkariotis.uniboost.dto.post.PostUserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private UUID id;

    private String username;

    private String firstname;

    private String lastname;

    private String email;

    private String phone;

    private List<PostUserResponseDto> postsOwnedByMe;
}
