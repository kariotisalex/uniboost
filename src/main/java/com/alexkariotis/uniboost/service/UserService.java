package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.domain.repository.UserRepository;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    public Try<User> findById(UUID id) {
        return Try.of(() -> Option.ofOptional(userRepository.findById(id))
                .getOrElseThrow(() -> new IllegalArgumentException("There is no user with this id " + id)));

    }


}
