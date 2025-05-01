package com.alexkariotis.uniboost.dto.post;

import com.alexkariotis.uniboost.dto.user.UserPostResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseOwnerDto {

    private UUID id;

    private String title;

    private String previewDescription;

    private String description;

    private Integer enrollments;

    private Integer maxEnrolls;

    private Boolean isPersonal;

    private String place;

    private List<UserPostResponseDto> enrolledStudents;
}
