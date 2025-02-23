package com.alexkariotis.uniboost.dto.post;

import com.alexkariotis.uniboost.domain.entity.User;

import java.util.List;

public class PostCreateDto {

    private String title;

    private String description;

    private Integer maxEnrolls;

    private Boolean isPersonal;

    private User userOwner;

    private List<User> enrolledUsers;

    public PostCreateDto() {
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getMaxEnrolls() {
        return this.maxEnrolls;
    }

    public Boolean getIsPersonal() {
        return this.isPersonal;
    }

    public User getUserOwner() {
        return this.userOwner;
    }

    public List<User> getEnrolledUsers() {
        return this.enrolledUsers;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaxEnrolls(Integer maxEnrolls) {
        this.maxEnrolls = maxEnrolls;
    }

    public void setIsPersonal(Boolean isPersonal) {
        this.isPersonal = isPersonal;
    }

    public void setUserOwner(User userOwner) {
        this.userOwner = userOwner;
    }

    public void setEnrolledUsers(List<User> enrolledUsers) {
        this.enrolledUsers = enrolledUsers;
    }

    public String toString() {
        return "PostCreateDto(title=" + this.getTitle() + ", description=" + this.getDescription() + ", maxEnrolls=" + this.getMaxEnrolls() + ", isPersonal=" + this.getIsPersonal() + ", userOwner=" + this.getUserOwner() + ", enrolledUsers=" + this.getEnrolledUsers() + ")";
    }
}
