package com.alexkariotis.uniboost.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "USER_")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    //    /**
    //     * One User can be the owner of post and can have many posts.
    //     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Post> postsOwnedByMe;

    /**
     * Many Users can be enrolled in many lesson offers.
     */
    @ManyToMany(mappedBy = "enrolledUsers",cascade = CascadeType.PERSIST)
    private List<Post> enrolledPosts;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;
}
