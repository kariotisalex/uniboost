package com.alexkariotis.uniboost.dto.post;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostCreateDto {

    private String title;

    private String previewDescription;

    private String description;

    private Integer maxEnrolls;

    private Boolean isPersonal;

    private String place;

}
