package com.alexkariotis.uniboost.common.exception;

public class PostNotFoundException extends IllegalArgumentException {
    public PostNotFoundException() {
    }

    public PostNotFoundException(String message) {
        super(message);
    }

    public PostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
