package com.alexkariotis.uniboost.mapper.user;

import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.dto.post.PostUserResponseDto;
import com.alexkariotis.uniboost.dto.user.UserResponseDto;

import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponseDto usertoUserResponseDto(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());

        // Mapping posts
        if (user.getPostsOwnedByMe() != null) {
            dto.setPostsOwnedByMe(user.getPostsOwnedByMe()
                    .stream()
                    .map(post -> {

                        PostUserResponseDto postUserResponseDto = new PostUserResponseDto();
                        postUserResponseDto.setId(post.getId());
                        postUserResponseDto.setTitle(post.getTitle());
                        postUserResponseDto.setDescription(post.getDescription());

                        return postUserResponseDto;
                    }).collect(Collectors.toList()));
        }

        return dto;
    }
}
