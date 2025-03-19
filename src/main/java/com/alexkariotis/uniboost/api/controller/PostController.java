package com.alexkariotis.uniboost.api.controller;

import com.alexkariotis.uniboost.common.Constants;
import com.alexkariotis.uniboost.dto.post.PostResponseContainerDto;
import com.alexkariotis.uniboost.dto.post.PostResponseDto;
import com.alexkariotis.uniboost.mapper.post.PostMapper;
import com.alexkariotis.uniboost.service.JwtService;
import com.alexkariotis.uniboost.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.POST)
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final JwtService jwtService;

    @GetMapping("test")
    public ResponseEntity<String> getPosts() {
        return ResponseEntity.ok("Hallo world!");
    }

    // todo This will allow to see only owner of posts
    /**
     * Fetch posts by searching or all posts paginated.
     * @param search Search field that searching for post.
     * @param page Page of pagination, default value is 0.
     * @param size Size of page of pagination, default value is 10.
     * @param sort Can choose the sort of the List.
     * @return if search field is empty, it will return full paginated list, otherwise it will return the result of
     *         the search process.
     */
    @GetMapping
    public ResponseEntity<PostResponseContainerDto> getPosts(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "title") String sort
    ) {
        log.info("PostController.getPosts()");
        return postService.findByTitle(search, page, size, sort)
                .map(post -> new PostResponseContainerDto(post
                        .stream()
                        .map(PostMapper::postToPostResponseDto)
                        .collect(Collectors.toList()),post.getTotalElements()))
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while fetching posts.", ex));
    }

    /**
     * Enroll a user to post.
     * @param auth retrieve from jwt token the username of logged in user.
     * @param postId post that the user will enroll.
     * @return The PostResponseDto that includes the new user.
     *
     */
    @PostMapping("{postId}/enroll/{userId}")
    public ResponseEntity<PostResponseDto> enroll(
            @RequestHeader("Authorization") String auth,
            @PathVariable("postId") UUID postId
    ) {

        String username = jwtService.extractUsername(auth.substring(7));
        log.info("PostController.enroll(), {} : {}", username, postId);
        return postService.enroll(username, postId)
                .map(PostMapper::postToPostResponseDto)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while enrolling user to post.", ex));
    }

    /**\
     * Disenroll user from post.
     * @param auth retrieve from jwt token the username of logged in user.
     * @param postId post that the user will disenroll.
     * @return The PostResponseDto that excludes the new user.
     */
    @PostMapping("{postId}/disenroll/{userId}")
    public ResponseEntity<PostResponseDto> disenroll(
            @RequestHeader("Authorization") String auth,
            @PathVariable("postId") UUID postId
    ) {
        String username = jwtService.extractUsername(auth.substring(7));
        log.info("PostController.disenroll(), {} : {}", username, postId);
        return postService.disenroll(username, postId)
                .map(PostMapper::postToPostResponseDto)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while disenrolling user to post.", ex));
    }



}
