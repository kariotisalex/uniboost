package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.common.exception.PostNotFoundException;
import com.alexkariotis.uniboost.common.exception.PostOwnershipException;
import com.alexkariotis.uniboost.domain.entity.Post;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.PostRepository;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import com.alexkariotis.uniboost.dto.SendEmailDto;
import com.alexkariotis.uniboost.dto.post.*;
import com.alexkariotis.uniboost.mapper.post.PostMapper;
import io.vavr.Tuple;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${link.post-details}")
    private String postDetailsLink = "";

    public Try<Page<Post>> findByTitle(String title, Integer page, Integer size, String sort, String username) {
        return Try.of(() -> postRepository
                .findByTitle(title.toLowerCase(), username, PageRequest.of(page, size, Sort.by(sort).descending())));
    }

    public Try<Page<Post>> findByUsername(int page, int size, String sort, String username) {
        return Try.of(() -> postRepository
                .findByUsername(username, PageRequest.of(page,size,Sort.by(sort).ascending())));
    }

    @Transactional
    public Try<PostDetailsResponseDto> enroll(final String username, final UUID postId) {
        return Try.of(() -> Option.ofOptional(postRepository.findById(postId))
                        .getOrElseThrow(() -> new PostNotFoundException("There is no post with id: " + postId)))
                .flatMap(post -> Try.of(() -> Option.ofOptional(userRepository.findByUsername(username))
                                .getOrElseThrow(() -> new IllegalArgumentException("There is no user with username: " + username)))
                        .flatMap(user -> {
                            io.vavr.collection.List<User> enrolledUsers = io.vavr.collection.List.ofAll(post.getEnrolledUsers());

                            return Try.of(() -> post)
                                    .filter(p -> p.getMaxEnrolls() > enrolledUsers.size(),
                                            () -> new IllegalStateException("Cannot enroll: The lesson is full."))
                                    .filter(p -> !p.getCreatedBy().equals(user),
                                            () -> new IllegalArgumentException("You cannot enroll if you are the owner of the post: " + username))
                                    .filter(p -> !enrolledUsers.contains(user),
                                            () -> new IllegalArgumentException("User already enrolled: " + username))
                                    .map(p -> {
                                        io.vavr.collection.List<User> updatedUsers = enrolledUsers.append(user);
                                        p.setEnrolledUsers(new ArrayList<>(updatedUsers.asJava()));
                                        return postRepository.saveAndFlush(p);
                                    }).map(post2 -> {

                                        // to new enrolled
                                        emailService.sendEmail(SendEmailDto.builder()
                                                .sendTo(user.getEmail())
                                                .subject("""
                                                        Enrollment Confirmed – You’ve been added to a new course!
                                                        """)
                                                .body("""
                                                        Dear %s,
                                                        
                                                        We are pleased to inform you that you have been successfully enrolled in the course: **%s**.
                                                        
                                                        You can access your course directly here:
                                                        %s
                                                        
                                                        The course is instructed by %s. Please check your student dashboard for materials and updates.
                                                        
                                                        This is an automated message sent from the Uniboost academic system. Replies to this email are not monitored.
                                                        
                                                        We wish you a productive learning experience.
                                                        
                                                        —
                                                        Uniboost Academic Services
                                                        """.formatted(user.getUsername(), post2.getTitle(),postDetailsLink+post2.getId(), post2.getCreatedBy().getFirstname()))
                                                .build());

                                        // to owner
                                        emailService.sendEmail(SendEmailDto.builder()
                                                .sendTo(post2.getCreatedBy().getEmail())//todo
                                                .subject("""
                                                        New Student Enrolled – %s has joined your course: %s"
                                                        """.formatted(user.getUsername(),post2.getTitle()))
                                                .body("""
                                                        
                                                        Dear %s,
                                                        
                                                        This is to notify you that %s has successfully enrolled in your course: **%s**.
                                                        
                                                        You may review the list of enrolled students and communicate any course-related instructions via the platform.
                                                        
                                                        This is an automated message from the Uniboost academic system. Please do not reply to this email.
                                                        
                                                        —
                                                        Uniboost Academic Services
                                                        
                                                        """.formatted(post2.getCreatedBy().getFirstname(), user.getFirstname() +" "+ user.getLastname(), post2.getTitle()))
                                                .build());
                                        return post2;
                                    });
                        }))
                .map(post -> {
                    return PostMapper.postToPostDetailsResponseDto(post, username);
                })
                .onFailure(Throwable::printStackTrace);
    }

    @Transactional
    public Try<PostDetailsResponseDto> disenroll(final String username, final UUID postId) {
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
                                    })
                                    .map(post2 -> {
                                        // to new enrolled
                                        emailService.sendEmail(SendEmailDto.builder()
                                                .sendTo(user.getEmail())
                                                .subject("""
                                                        Disenrollment Notification – You’ve been removed from a course
                                                        """)
                                                .body("""
                                                        Dear %s,
                                                        
                                                        This is to inform you that you have been disenrolled from the course: **%s**.
                                                                                                                
                                                        If you believe this was a mistake or require further information, please reach out to the course instructor.
                                                                                                                
                                                        This is an automated message from the Uniboost academic system. Please do not reply to this email.
                                                                                                                
                                                        —
                                                        Uniboost Academic Services
                                                        """.formatted(user.getFirstname(), post2.getTitle()))
                                                .build());

                                        // to owner
                                        emailService.sendEmail(SendEmailDto.builder()
                                                .sendTo(post2.getCreatedBy().getEmail())
                                                .subject("""
                                                        Student Disenrolled – %s has left your course: %s
                                                        """.formatted(user.getUsername(),post2.getTitle()))
                                                .body("""
                                                        
                                                        Dear %s,
                                                        
                                                        This is to inform you that the student **%s** has been disenrolled from your course: **%s**.
                                                        
                                                        You may review your current student list via your course dashboard. No action is required on your part unless further follow-up is necessary.
                                                        
                                                        This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                                        
                                                        —
                                                        Uniboost Academic Services
                                                        
                                                        """.formatted(post2.getCreatedBy().getFirstname(), user.getFirstname() +" "+ user.getLastname(), post2.getTitle()))
                                                .build());

                                        return post2;
                                    });
                        })).map(post -> PostMapper.postToPostDetailsResponseDto(post, username));
    }

    @Transactional
    public Try<UUID> create(PostCreateDto createDto, String username) {
        return Try.of(() -> Option.ofOptional(userRepository.findByUsername(username))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no user with username: " + username)))
                .filter(user -> user.getPostsOwnedByMe().stream().noneMatch(post -> Objects.equals(post.getTitle(), createDto.getTitle()))
                ,() ->new IllegalArgumentException("The title is already use by this user."))
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
                }).map(post ->{
                    emailService.sendEmail(SendEmailDto
                            .builder()
                            .sendTo(post.getCreatedBy().getEmail())
                            .subject("""
                                    Course Created – "%s" is now active on Uniboost
                                    """.formatted(post.getTitle()))
                            .body("""
                                    Dear %s,
                                                                        
                                    Congratulations! Your new course, **%s**, has been successfully created and is now active on the Uniboost platform.
                                                                        
                                    You can manage your course through your instructor dashboard, including adding materials, setting max enrollments, and tracking student progress.
                                                                        
                                    Students may begin enrolling as soon as the course appears in the feed.
                                                                        
                                    Thank you for contributing to the Uniboost academic network.
                                                                        
                                    This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                                                        
                                    —
                                    Uniboost Academic Services
                                    """.formatted(post.getCreatedBy().getFirstname(),post.getTitle()))
                            .build());
                    return post.getId();
                })
                .onFailure(Throwable::printStackTrace);

    }
    @Transactional
    public Try<PostResponseOwnerDto> update(PostUpdateDto updateDto, String username) {
        return Try.of(() -> Option.ofOptional(postRepository.findById(updateDto.getId()))
                        .getOrElseThrow(() -> new IllegalArgumentException(
                                "There is no post with id: "+updateDto.getId())))
                .filter(post -> Objects.equals(post.getCreatedBy().getUsername(), username),
                        () -> new PostOwnershipException("Updating of this post is not possible due to user is not the owner of the post!"))

                        .map(post -> {
                            post.setPreviewDescription(updateDto.getPreviewDescription());
                            post.setDescription(updateDto.getDescription());
                            post.setMaxEnrolls(updateDto.getMaxEnrolls());
                            post.setIsPersonal(updateDto.getIsPersonal());
                            post.setPlace(updateDto.getPlace());
                            post.setUpdatedAt(OffsetDateTime.now());
                            return postRepository.saveAndFlush(post);
                        })
                            .map(post2 -> {
                                post2.getEnrolledUsers().forEach(user -> {
                                    // to new enrolled
                                    emailService.sendEmail(SendEmailDto.builder()
                                            .sendTo(user.getEmail())
                                            .subject("""
                                        Course Update – \\"%s\\" has been updated
                                        """.formatted(post2.getTitle()))
                                            .body("""
                                        Dear %s,
                                        
                                        We’d like to inform you that your enrolled course, **%s**, has recently been updated by the instructor.
                                        
                                        Changes may include updates to the schedule, location, or course materials.
                                        
                                        You can access your course directly here:
                                        %s
                                        
                                        We recommend that you review the course page to stay informed and up to date.
                                        
                                        This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                        
                                        —
                                        Uniboost Academic Services
                                        """.formatted(user.getFirstname(), post2.getTitle(), postDetailsLink+post2.getId()))
                                            .build());
                                });


                        // to owner
                        emailService.sendEmail(SendEmailDto.builder()
                                .sendTo(post2.getCreatedBy().getEmail())
                                .subject("""
                                        Course Updated – \\"%s\\" changes saved successfully
                                        """.formatted(post2.getTitle()))
                                .body("""
                                        Dear %s,
                                        
                                        This is to confirm that your recent changes to the course **%s** have been saved successfully.
                                        
                                        All enrolled students have been notified of the update. You may continue managing your course from the dashboard.
                                        
                                        This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                        
                                        —
                                        Uniboost Academic Services
                                        
                                        """.formatted(post2.getCreatedBy().getFirstname(), post2.getTitle()))
                                .build());

                        return post2;
                    }).map(PostMapper::postToPostResponseOwnerDto)
                    .onFailure(Throwable::printStackTrace);
    }
    @Transactional
    public Try<Void> delete(String username, UUID postId) {
        return Try.of(() -> Option.ofOptional(postRepository.findById(postId))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .filter(post -> post.getCreatedBy().getUsername().equals(username),
                        () -> new IllegalArgumentException(
                                "Deleting of this post is not possible due to user is not the owner of the post!"))
                .flatMap(post -> Try.run(() -> postRepository.deleteById(postId)).map(ignored -> post))
                .map(post2 -> {
                    post2.getEnrolledUsers().forEach(user -> {
                        // to new enrolled
                        emailService.sendEmail(SendEmailDto.builder()
                                .sendTo(user.getEmail())
                                .subject("""
                                        Course Removed – \\"%s\\" is no longer available
                                        """.formatted(post2.getTitle()))
                                .body("""
                                    Dear %s,
                                    
                                    We’d like to inform you that the course **%s**, in which you were previously enrolled, has been deleted by the instructor.
                                    
                                    As a result, this course is no longer available in your dashboard.
                                    
                                    If you have any questions or require clarification, please contact your instructor through the platform.
                                    
                                    This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                    
                                    —
                                    Uniboost Academic Services
                                    """.formatted(user.getFirstname(), post2.getTitle()))
                                .build());
            });


            // to owner
            emailService.sendEmail(SendEmailDto.builder()
                    .sendTo(post2.getCreatedBy().getEmail())
                    .subject("""
                            Course Deleted – \\"%s\\" has been successfully removed
                            """.formatted(post2.getTitle()))
                    .body("""
                        Dear %s,    
                        
                        This is to confirm that your course **%s** has been permanently deleted from the system.
                        
                        All enrolled students have been notified of this change, and the course has been removed from their dashboards.
                        
                        If you wish to re-offer this course in the future, you may create a new course at any time via your dashboard.
                        
                        This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                        
                        —
                        Uniboost Academic Services
                                        
                        """.formatted(post2.getCreatedBy().getFirstname(), post2.getTitle()))
                    .build());

            return null;
        });

    }
    @Transactional
    public Try<Void> deleteEnrolledStudent(String username, UUID postId, UUID userId) {
        return Try.of(() -> Option.ofOptional(postRepository.findById(postId))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .filter(post -> post.getCreatedBy().getUsername().equals(username),
                        () -> new IllegalArgumentException(
                                "Deleting the enrolled student from post is " +
                                        "not possible due to user is not the owner of the post!"))
                .map(post -> {

                    post.setEnrolledUsers(post
                            .getEnrolledUsers()
                            .stream()
                            .filter(user -> !user.getId().equals(userId))
                            .collect(Collectors.toList()));
                    return postRepository.saveAndFlush(post);
                }).onFailure(Throwable::printStackTrace)
                .map(post ->  Tuple.of(Option.ofOptional(userRepository.findById(userId))
                                .getOrElseThrow(() -> new IllegalArgumentException("there is no user with id :" + userId))
                        , post)
                )
                .map(tuple2 -> {
                    User user = tuple2._1;
                    Post post = tuple2._2;

                    // to new enrolled
                    emailService.sendEmail(SendEmailDto.builder()
                            .sendTo(user.getEmail())
                            .subject("""
                                    Enrollment Update – You’ve been removed from the course: %s
                                    """.formatted(post.getTitle()))
                            .body("""
                                    Dear %s,
                                                                                            
                                    This is to inform you that you have been removed from the course: **%s** by the course instructor.
                                                                                            
                                    If you believe this was a mistake or you have questions regarding your enrollment, please reach out to the instructor directly via the platform.
                                                                                            
                                    This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                                                                            
                                    —
                                    Uniboost Academic Services
                                    """.formatted(user.getFirstname(), post.getTitle()))
                            .build());

                    // to owner
                    emailService.sendEmail(SendEmailDto.builder()
                            .sendTo(post.getCreatedBy().getEmail())
                            .subject("""
                                    Student Disenrolled – %s has left your course: %s
                                    """.formatted(user.getUsername(),post.getTitle()))
                            .body("""
                                                        
                            Dear %s,
                                                                                
                            This is to confirm that you have successfully removed the student **%s** from your course: **%s**.
                                                                                
                            Your course roster has been updated. If this action was performed in error, you may re-enroll the student manually through the course dashboard.
                                                                                
                            This is an automated message sent by the Uniboost academic system. Please do not reply to this email.
                                                                                
                            —
                            Uniboost Academic Services
                            """.formatted(post.getCreatedBy().getFirstname(),
                                    user.getFirstname() +" "+ user.getLastname(), post.getTitle()))
                            .build());

                    return null;
                });

    }

    public Try<PostDetailsResponseDto> getPostById(String username, UUID postId) {

        return Try.of(() -> Option.ofOptional(postRepository.findById(postId))
                .getOrElseThrow((() -> new IllegalArgumentException("There is no post with id: " + postId))))
                .map(post -> PostMapper.postToPostDetailsResponseDto(post,username))
                .onFailure(Throwable::printStackTrace);
    }

    public Try<PostResponseContainerDto> findEnrolledByUsername(int page, int size, String sort, String username) {
        return Try.of(() -> postRepository.findEnrolledPostsByUsername(username, PageRequest.of(page,size,Sort.by(sort).ascending())))
                .map(posts -> new PostResponseContainerDto(
                                    posts.stream().map(PostMapper::postToPostResponseDto).toList(),
                                    posts.getTotalElements())
                );

    }

    public Try<PostResponseOwnerDto> getMyPostById(UUID postId, String username) {
        return Try.of(() -> Option.ofOptional(postRepository.findByIdAndUsername(postId, username))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no post with id: " + postId)))
                .map(PostMapper::postToPostResponseOwnerDto);
    }
}
