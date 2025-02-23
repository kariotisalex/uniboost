package com.alexkariotis.uniboost.dto.post;

import com.alexkariotis.uniboost.dto.user.UserResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PostResponseDto {

    private UUID id;

    private String title;

    private String description;

    private Integer maxEnrolls;

    private Boolean isPersonal;

    private UserResponseDto userOwner;

    private List<UserResponseDto> enrolledUsers;
}
