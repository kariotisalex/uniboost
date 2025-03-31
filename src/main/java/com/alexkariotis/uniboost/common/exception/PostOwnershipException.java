package com.alexkariotis.uniboost.common.exception;

public class PostOwnershipException extends IllegalArgumentException{
    public PostOwnershipException() {
    }

    public PostOwnershipException(String s) {
        super(s);
    }

    public PostOwnershipException(String message, Throwable cause) {
        super(message, cause);
    }
}
