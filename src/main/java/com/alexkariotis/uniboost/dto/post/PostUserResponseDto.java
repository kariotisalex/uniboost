package com.alexkariotis.uniboost.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserResponseDto {

    private UUID id;

    private String title;

    private String description;

}
