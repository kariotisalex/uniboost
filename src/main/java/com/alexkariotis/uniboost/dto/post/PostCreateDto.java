package com.alexkariotis.uniboost.dto.post;

import com.alexkariotis.uniboost.domain.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostCreateDto {

    private String title;

    private String description;

    private Integer maxEnrolls;

    private Boolean isPersonal;

    private User userOwner;

    private List<User> enrolledUsers;
}
