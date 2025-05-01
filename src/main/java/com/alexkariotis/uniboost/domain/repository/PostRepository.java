package com.alexkariotis.uniboost.domain.repository;

import com.alexkariotis.uniboost.domain.entity.Post;
import com.alexkariotis.uniboost.dto.post.PostResponseOwnerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {


    Optional<Post> findById(@NonNull UUID id);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE LOWER( p.title) LIKE %:title% AND p.createdBy.username != :username")
    Page<Post> findByTitle(@Param("title")String title, @Param("username") String username, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdBy.username=:username")
    Page<Post> findByUsername(@Param("username")String username, Pageable pageable);

    @Query("""
    SELECT p FROM User u
    JOIN u.enrolledPosts p
    WHERE u.username = :username
    """)
    Page<Post> findEnrolledPostsByUsername(@Param("username") String username, Pageable pageable);

    @Query("""
        SELECT p
        FROM Post p
        WHERE p.createdBy.username = :username AND p.id = :postId
""")
    Optional<Post> findByIdAndUsername(@Param("postId")UUID postId, @Param("username") String username);
}
