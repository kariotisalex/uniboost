package com.alexkariotis.uniboost.api.controller;

import com.alexkariotis.uniboost.common.Constants;
import com.alexkariotis.uniboost.dto.post.PostResponseDto;
import com.alexkariotis.uniboost.mapper.post.PostMapper;
import com.alexkariotis.uniboost.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.API)
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getPost() {
        return postService.findAll()
                .map(post -> post
                        .stream()
                        .map(PostMapper::postToPostResponseDto)
                        .collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .get();
    }

    @PostMapping("{postId}/enroll/{userId}")
    public ResponseEntity<PostResponseDto> enroll(
            @PathVariable("userId") UUID userId,
            @PathVariable("postId") UUID postId
    ) throws Throwable {
        System.out.println(userId);
        System.out.println(postId);
        return postService.enroll(userId, postId)
                .map(PostMapper::postToPostResponseDto)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> ex);
    }

    @PostMapping("{postId}/disenroll/{userId}")
    public ResponseEntity<PostResponseDto> disenroll(
            @PathVariable("userId") UUID userId,
            @PathVariable("postId") UUID postId
    ) throws Throwable {
        System.out.println(userId);
        System.out.println(postId);
        return postService.disenroll(userId, postId)
                .map(PostMapper::postToPostResponseDto)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> ex);
    }



}
