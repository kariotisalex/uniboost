package com.alexkariotis.uniboost.mapper.user;

import com.alexkariotis.uniboost.common.RoleEnum;
import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.dto.user.UserCreateDto;
import com.alexkariotis.uniboost.dto.user.UserPostResponseDto;

public class UserMapper {

    public static UserPostResponseDto usertoUserPostResponseDto(User user) {
        if (user == null) {
            return null;
        }

        UserPostResponseDto dto = new UserPostResponseDto();
        dto.setUsername(user.getUsername());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());

        return dto;
    }

    public static User userCreateDtoToUser(UserCreateDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setFirstname(dto.getFirstName());
        user.setLastname(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(RoleEnum.USER);
        return user;
    }
}
