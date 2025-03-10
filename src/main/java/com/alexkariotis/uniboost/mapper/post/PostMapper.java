package com.alexkariotis.uniboost.mapper.post;

import com.alexkariotis.uniboost.domain.entity.Post;
import com.alexkariotis.uniboost.dto.post.PostResponseDto;
import com.alexkariotis.uniboost.dto.user.UserPostResponseDto;

public class PostMapper {


    public static PostResponseDto postToPostResponseDto(Post post) {
        if (post == null) {
            return null;
        } else {
            PostResponseDto responseDto = new PostResponseDto();
            responseDto.setId(post.getId());
            responseDto.setTitle(post.getTitle());
            responseDto.setDescription(post.getDescription());
            responseDto.setMaxEnrolls(post.getMaxEnrolls());
            responseDto.setIsPersonal(post.getIsPersonal());
            responseDto.setPlace(post.getPlace());

            UserPostResponseDto createdByDto = new UserPostResponseDto();
            createdByDto.setId(post.getCreatedBy().getId());
            createdByDto.setUsername(post.getCreatedBy().getUsername());
            createdByDto.setFirstname(post.getCreatedBy().getFirstname());
            createdByDto.setLastname(post.getCreatedBy().getLastname());
            createdByDto.setEmail(post.getCreatedBy().getEmail());
            createdByDto.setPhone(post.getCreatedBy().getPhone());



            responseDto.setUserOwner(createdByDto);
            responseDto.setEnrolledUsers(post.getEnrolledUsers()
                    .stream()
                    .map(user -> {
                        UserPostResponseDto userDto = new UserPostResponseDto();
                        userDto.setId(user.getId());
                        userDto.setUsername(user.getUsername());
                        userDto.setFirstname(user.getFirstname());
                        userDto.setLastname(user.getLastname());
                        userDto.setEmail(user.getEmail());
                        userDto.setPhone(user.getPhone());
                        return userDto;
                    })
                    .toList());
            return responseDto;
        }
    }

}
