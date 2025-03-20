package com.alexkariotis.uniboost.mapper.post;

import com.alexkariotis.uniboost.domain.entity.Post;
import com.alexkariotis.uniboost.dto.post.PostCreateDto;
import com.alexkariotis.uniboost.dto.post.PostResponseDto;
import com.alexkariotis.uniboost.dto.post.PostResponseOwnerDto;
import com.alexkariotis.uniboost.dto.post.PostUpdateDto;
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
            createdByDto.setUsername(post.getCreatedBy().getUsername());
            createdByDto.setFirstname(post.getCreatedBy().getFirstname());
            createdByDto.setLastname(post.getCreatedBy().getLastname());
            createdByDto.setEmail(post.getCreatedBy().getEmail());
            createdByDto.setPhone(post.getCreatedBy().getPhone());

            responseDto.setUserOwner(createdByDto);
            responseDto.setEnrollments(post.getEnrolledUsers().size());
            return responseDto;
        }
    }

    public static PostResponseOwnerDto postToPostResponseOwnerDto(Post post) {
        if (post == null) {
            return null;
        } else {
            PostResponseOwnerDto responseDto = new PostResponseOwnerDto();
            responseDto.setId(post.getId());
            responseDto.setTitle(post.getTitle());
            responseDto.setDescription(post.getDescription());
            responseDto.setMaxEnrolls(post.getMaxEnrolls());
            responseDto.setIsPersonal(post.getIsPersonal());
            responseDto.setPlace(post.getPlace());

            UserPostResponseDto createdByDto = new UserPostResponseDto();
            createdByDto.setUsername(post.getCreatedBy().getUsername());
            createdByDto.setFirstname(post.getCreatedBy().getFirstname());
            createdByDto.setLastname(post.getCreatedBy().getLastname());
            createdByDto.setEmail(post.getCreatedBy().getEmail());
            createdByDto.setPhone(post.getCreatedBy().getPhone());


            responseDto.setEnrolledStudents(post.getEnrolledUsers()
                    .stream()
                    .map(user -> {
                        UserPostResponseDto userDto = new UserPostResponseDto();
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

    public static PostCreateDto postToPostCreateDto(Post post) {
        if (post == null) {
            return null;
        } else {
            PostCreateDto createDto = new PostCreateDto();
            createDto.setTitle(post.getTitle());
            createDto.setDescription(post.getDescription());
            createDto.setMaxEnrolls(post.getMaxEnrolls());
            createDto.setIsPersonal(post.getIsPersonal());
            createDto.setPlace(post.getPlace());
            return createDto;
        }
    }

    public static PostUpdateDto postToPostUpdateDto(Post post) {
        if (post == null) {
            return null;
        } else {
            PostUpdateDto updateDto = new PostUpdateDto();
            updateDto.setId(post.getId());
            updateDto.setTitle(post.getTitle());
            updateDto.setDescription(post.getDescription());
            updateDto.setMaxEnrolls(post.getMaxEnrolls());
            updateDto.setIsPersonal(post.getIsPersonal());
            updateDto.setPlace(post.getPlace());
            return updateDto;
        }
    }
}
