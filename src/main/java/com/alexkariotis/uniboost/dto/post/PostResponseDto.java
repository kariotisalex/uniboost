package com.alexkariotis.uniboost.dto.post;

import com.alexkariotis.uniboost.dto.user.UserPostResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

    private String place;

    private UserPostResponseDto userOwner;

    private Integer enrollments;
}
