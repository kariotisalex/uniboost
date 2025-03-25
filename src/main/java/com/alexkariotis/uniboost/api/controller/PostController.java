package com.alexkariotis.uniboost.api.controller;

import com.alexkariotis.uniboost.common.Constants;
import com.alexkariotis.uniboost.dto.post.*;
import com.alexkariotis.uniboost.mapper.post.PostMapper;
import com.alexkariotis.uniboost.api.filter.utils.JwtUtils;
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
    private final JwtUtils jwtUtils;

    /**
     * Fetch posts by searching or all posts paginated without fetching posts created
     * by the client who asking for fetching.
     * @param search Search field that searching for post.
     * @param page Page of pagination, default value is 0.
     * @param size Size of page of pagination, default value is 10.
     * @param sort Can choose the sort of the List.
     * @param auth retrieve from jwt token the username of logged in user.
     * @return if search field is empty, it will return full paginated list, otherwise it will return the result of
     *         the search process.
     */
    @GetMapping("feed")
    public ResponseEntity<PostResponseContainerDto> getPosts(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "page"  , required = false, defaultValue = "0") int page,
            @RequestParam(name = "size"  , required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort"  , required = false, defaultValue = "updatedAt") String sort,
            @RequestHeader("Authorization") String auth

    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));

        log.info("PostController.getPosts()");
        return postService.findByTitle(search, page, size, sort, username)
                .map(post -> new PostResponseContainerDto(post
                        .stream()
                        .map(PostMapper::postToPostResponseDto)
                        .collect(Collectors.toList()), post.getTotalElements()))
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while fetching posts.", ex));
    }

    /**
     * Fetching posts produced by client who asking for fetching.
     * @param page Page of pagination, default value is 0.
     * @param size Size of page of pagination, default value is 10.
     * @param sort Can choose the sort of the List.
     * @param auth retrieve from jwt token the username of logged in user.
     * @return a container with paginated list and total number of elements.
     */
    @GetMapping ("myposts")
    public ResponseEntity<PostResponseOwnerContainerDto> getOwnersPosts(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "updatedAt") String sort,
            @RequestHeader("Authorization") String auth
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.getOwnersPosts()");

        return postService.findByUsername(page, size, sort, username)
                .map(posts -> new PostResponseOwnerContainerDto(
                        posts.stream().map(PostMapper::postToPostResponseOwnerDto).collect(Collectors.toList()),
                        posts.getTotalElements()))
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while fetching owners.", ex));

    }

    @PostMapping()
    public ResponseEntity<PostCreateDto> create(
            @RequestBody PostCreateDto createDto,
            @RequestHeader("Authorization") String auth
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.create()");

        return postService.create(createDto, username)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while creating post.", ex));
    }


    @PutMapping
    public ResponseEntity<PostUpdateDto> update(
            @RequestBody PostUpdateDto updateDto,
            @RequestHeader("Authorization") String auth
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.update()");

        return postService.update(updateDto, username)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while updating post.", ex));
    }

    @DeleteMapping("{postId}")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String auth,
            @PathVariable("postId") UUID postId
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));

        log.info("PostController.delete()");
        return postService
                .delete(username, postId)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while deleting post.", ex));
    }


    @DeleteMapping("{userId}/{postId}")
    public ResponseEntity<Void> deleteEnrolledStudent(
            @RequestHeader("Authorization") String auth,
            @PathVariable("postId") UUID postId,
            @PathVariable("userId") UUID userId
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.deleteEnrolledStudent()");

        return postService
                .deleteEnrolledStudent(username,postId,userId)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex ->
                        new RuntimeException("Something went wrong while deleting enrolled student from post.", ex));
    }


    /**
     * Enroll a user to post.
     * @param auth retrieve from jwt token the username of logged in user.
     * @param postId post that the user will enroll.
     * @return The PostResponseDto that includes the new user.
     *
     */
    @PostMapping("enroll/{postId}")
    public ResponseEntity<PostResponseDto> enroll(
            @RequestHeader("Authorization") String auth,
            @PathVariable("postId") UUID postId
    ) {

        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.enroll(), {} : {}", username, postId);
        return postService.enroll(username, postId)
                .map(PostMapper::postToPostResponseDto)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while enrolling user to post.", ex));
    }

    /**
     * Disenroll user from post.
     * @param auth retrieve from jwt token the username of logged in user.
     * @param postId post that the user will disenroll.
     * @return The PostResponseDto that excludes the new user.
     */
    @PostMapping("disenroll/{postId}")
    public ResponseEntity<PostResponseDto> disenroll(
            @RequestHeader("Authorization") String auth,
            @PathVariable("postId") UUID postId
    ) {
        String username = jwtUtils.extractUsername(auth.substring(7));
        log.info("PostController.disenroll(), {} : {}", username, postId);
        return postService.disenroll(username, postId)
                .map(PostMapper::postToPostResponseDto)
                .map(ResponseEntity::ok)
                .getOrElseThrow(ex -> new RuntimeException("Something went wrong while disenrolling user to post.", ex));
    }



}
