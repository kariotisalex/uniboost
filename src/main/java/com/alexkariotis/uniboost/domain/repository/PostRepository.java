package com.alexkariotis.uniboost.domain.repository;

import com.alexkariotis.uniboost.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findById(UUID id);


}
