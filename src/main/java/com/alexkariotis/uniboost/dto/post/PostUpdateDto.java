package com.alexkariotis.uniboost.dto.post;

import com.alexkariotis.uniboost.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDto {

    private UUID id;

    private String title;

    private String description;

    private Integer maxEnrolls;

    private Boolean isPersonal;

    private String place;
}
