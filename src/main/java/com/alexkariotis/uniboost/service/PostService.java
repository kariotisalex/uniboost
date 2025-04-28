package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.common.exception.PostNotFoundException;
import com.alexkariotis.uniboost.common.exception.PostOwnershipException;
import com.alexkariotis.uniboost.domain.entity.Post;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.PostRepository;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import com.alexkariotis.uniboost.dto.post.PostCreateDto;
import com.alexkariotis.uniboost.dto.post.PostDetailsResponseDto;
import com.alexkariotis.uniboost.dto.post.PostResponseDto;
import com.alexkariotis.uniboost.dto.post.PostUpdateDto;
import com.alexkariotis.uniboost.mapper.post.PostMapper;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final UserRepository userRepository;

    public Try<Page<Post>> findByTitle(String title, Integer page, Integer size, String sort, String username) {
        return Try.of(() -> postRepository
                .findByTitle(title.toLowerCase(), username, PageRequest.of(page, size, Sort.by(sort).ascending())));
    }

    public Try<Page<Post>> findByUsername(int page, int size, String sort, String username) {
        return Try.of(() -> postRepository
                .findByUsername(username, PageRequest.of(page,size,Sort.by(sort).ascending())));
    }

    @Transactional
    public Try<Post> enroll(final String username, final UUID postId) {
        return Try.of(() -> postRepository.findById(postId)) // d
                .flatMap(postOpt -> Option.ofOptional(postOpt)
                        .toTry(() -> new PostNotFoundException("There is no post with id: " + postId)))
                .flatMap(post -> Try.of(() -> userRepository.findByUsername(username))
                        .flatMap(userOpt -> Option.ofOptional(userOpt)
                                .toTry(() -> new UsernameNotFoundException("There is no user with username: " + username)))
                        .flatMap(user -> {
                            io.vavr.collection.List<User> enrolledUsers = io.vavr.collection.List.ofAll(post.getEnrolledUsers());

                            return Try.of(()-> post)
                                    .filter(p -> p.getMaxEnrolls() > enrolledUsers.size(),
                                            () -> new IllegalStateException("Cannot enroll: The lesson is full."))
                                    .filter(p -> !p.getCreatedBy().equals(user),
                                            () -> new IllegalArgumentException("You cannot enroll if you are the owner of post" + username))
                                    .filter(p -> !enrolledUsers.contains(user),
                                            () -> new IllegalArgumentException("You have enrolled the user with id: " + username))
                                    .map(p -> {
                                        enrolledUsers.append(user);
                                        p.setEnrolledUsers(enrolledUsers.asJava());
                                        postRepository.flush();
                                        return postRepository.saveAndFlush(p);
                                    });
                        }))
                .onFailure(Throwable::printStackTrace);
    }

    @Transactional
    public Try<Post> disenroll(final String username, final UUID postId) {
        return Try.of(() -> postRepository.findById(postId))
                .flatMap(postOpt -> Option.ofOptional(postOpt)
                        .toTry(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .flatMap(post -> Try.of(() -> userRepository.findByUsername(username))
                        .flatMap(userOpt -> Option.ofOptional(userOpt)
                                .toTry(() -> new IllegalArgumentException("There is no user with id: " + username)))
                        .flatMap(user -> {
                            java.util.List<User> enrolledUsers = new ArrayList<>(post.getEnrolledUsers());

                            return Try.success(post)
                                    .filter(p -> enrolledUsers.contains(user),
                                            () -> new IllegalArgumentException(
                                                    "User with username: " + username +
                                                            " is not enrolled in post: " + postId))
                                    .map(p -> {
                                        enrolledUsers.remove(user);
                                        p.setEnrolledUsers(enrolledUsers);
                                        postRepository.flush();
                                        return postRepository.saveAndFlush(p);
                                    });
                        }));
    }

    @Transactional
    public Try<PostCreateDto> create(PostCreateDto createDto, String username) {
        return Try.of(() -> Option.ofOptional(userRepository.findByUsername(username))
                .getOrElseThrow(() -> new UsernameNotFoundException("There is no user with username: " + username)))
                .map(user -> {
                    Post post = new Post();
                    post.setId(UUID.randomUUID());
                    post.setTitle(createDto.getTitle());
                    post.setPreviewDescription(createDto.getPreviewDescription());
                    post.setDescription(createDto.getDescription());
                    post.setMaxEnrolls(createDto.getMaxEnrolls());
                    post.setIsPersonal(createDto.getIsPersonal());
                    post.setPlace(createDto.getPlace());
                    post.setCreatedBy(user);
                    post.setCreatedAt(OffsetDateTime.now());
                    post.setUpdatedAt(OffsetDateTime.now());
                    return postRepository.saveAndFlush(post);
                })
                .map(PostMapper::postToPostCreateDto)
                .onFailure(Throwable::printStackTrace);

    }
    @Transactional
    public Try<PostUpdateDto> update(PostUpdateDto updateDto, String username) {
        return Try.of(() -> Option.ofOptional(postRepository.findById(updateDto.getId()))
                        .getOrElseThrow(() -> new IllegalArgumentException(
                                "There is no post with id: "+updateDto.getId())))
                .filter(post -> Objects.equals(post.getCreatedBy().getUsername(), username),
                        () -> new PostOwnershipException("Updating of this post is not possible due to user is not the owner of the post!"))

                        .map(post -> {
                            post.setTitle(updateDto.getTitle());
                            post.setDescription(updateDto.getDescription());
                            post.setMaxEnrolls(updateDto.getMaxEnrolls());
                            post.setIsPersonal(updateDto.getIsPersonal());
                            post.setPlace(updateDto.getPlace());
                            post.setUpdatedAt(OffsetDateTime.now());
                            return postRepository.saveAndFlush(post);
                        })
                        .map(PostMapper::postToPostUpdateDto)
                        .onFailure(Throwable::printStackTrace);
    }
    @Transactional
    public Try<Void> delete(String username, UUID postId) {
        return Try.of(() -> Option.ofOptional(postRepository.findById(postId))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .filter(post -> post.getCreatedBy().getUsername().equals(username),
                        () -> new IllegalArgumentException(
                                "Deleting of this post is not possible due to user is not the owner of the post!"))
                .flatMap(post -> Try.run(()-> postRepository.deleteById(postId)));

    }
    @Transactional
    public Try<Void> deleteEnrolledStudent(String username, UUID postId, UUID userId) {
        return Try.of(() -> Option.ofOptional(postRepository.findById(postId))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .filter(post -> post.getCreatedBy().getUsername().equals(username),
                        () -> new IllegalArgumentException(
                                "Deleting of enrolled student of post is not possible due to user is not the owner of the post!"))
                .map(post -> {

                    post.setEnrolledUsers(post
                            .getEnrolledUsers()
                            .stream()
                            .filter(user -> !user.getId().equals(userId))
                            .collect(Collectors.toList()));
                    return postRepository.saveAndFlush(post);
                }).onFailure(Throwable::printStackTrace)
                .map(post -> null);
//                ;

    }

    public Try<PostDetailsResponseDto> getPostById(String username, UUID postId) {

        return Try.of(() -> Option.ofOptional(postRepository.findById(postId))
                .getOrElseThrow((() -> new IllegalArgumentException("There is no post with id: " + postId))))
                .map(post -> PostMapper.postToPostDetailsResponseDto(post,username))
                .onFailure(Throwable::printStackTrace);
    }
}
