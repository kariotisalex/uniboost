package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.domain.entity.Post;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.PostRepository;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final UserRepository userRepository;

    public Try<Page<Post>> findByTitle(String title, Integer page, Integer size, String sort) {
        return Try.of(() -> postRepository.findByTitle(title.toLowerCase(), PageRequest.of(page, size, Sort.by(sort).ascending())));
    }

    public Try<Post> enroll(final UUID userId, final UUID postId) {
        return Try.of(() -> postRepository.findById(postId))
                .flatMap(postOpt -> Option.ofOptional(postOpt)
                        .toTry(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .flatMap(post -> Try.of(() -> userRepository.findById(userId))
                        .flatMap(userOpt -> Option.ofOptional(userOpt)
                                .toTry(() -> new IllegalArgumentException("There is no user with id: " + userId)))
                        .flatMap(user -> {
                            io.vavr.collection.List<User> enrolledUsers = io.vavr.collection.List.ofAll(post.getEnrolledUsers());

                            return Try.of(()-> post)
                                    .filter(p -> p.getMaxEnrolls() > enrolledUsers.size(),
                                            () -> new IllegalStateException("Cannot enroll: The lesson is full."))
                                    .filter(p -> !p.getCreatedBy().equals(user),
                                            () -> new IllegalArgumentException("You cannot enroll if you are the owner of post" + userId))
                                    .filter(p -> !enrolledUsers.contains(user),
                                            () -> new IllegalArgumentException("You have enrolled the user with id: " + userId))
                                    .map(p -> {
                                        enrolledUsers.append(user);
                                        p.setEnrolledUsers(enrolledUsers.asJava());
                                        postRepository.flush();
                                        return postRepository.saveAndFlush(p);
                                    });
                        }))
                .onFailure(Throwable::printStackTrace);
    }

    public Try<Post> disenroll(UUID userId, UUID postId) {
        return Try.of(() -> postRepository.findById(postId))
                .flatMap(postOpt -> Option.ofOptional(postOpt)
                        .toTry(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .flatMap(post -> Try.of(() -> userRepository.findById(userId))
                        .flatMap(userOpt -> Option.ofOptional(userOpt)
                                .toTry(() -> new IllegalArgumentException("There is no user with id: " + userId)))
                        .flatMap(user -> {
                            java.util.List<User> enrolledUsers = new ArrayList<>(post.getEnrolledUsers());

                            return Try.success(post)
                                    .filter(p -> enrolledUsers.contains(user),
                                            () -> new IllegalArgumentException("User with id: " + userId + " is not enrolled in post: " + postId))
                                    .map(p -> {
                                        enrolledUsers.remove(user);
                                        p.setEnrolledUsers(enrolledUsers);
                                        postRepository.flush();
                                        return postRepository.saveAndFlush(p);
                                    });
                        }));
    }
}
