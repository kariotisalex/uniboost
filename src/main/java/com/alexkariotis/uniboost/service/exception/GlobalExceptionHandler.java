package com.alexkariotis.uniboost.service.exception;

import com.alexkariotis.uniboost.common.exception.PostOwnershipException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception ex) {
        return new ResponseEntity<>(
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUsernameNotFoundException(Exception ex) {
        return new ResponseEntity<>(
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(Exception ex) {
        return new ResponseEntity<>(
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(Exception ex) {
        return new ResponseEntity<>(
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED,ex.getMessage()),
                HttpStatus.NOT_IMPLEMENTED
        );
    }

    @ExceptionHandler(PostOwnershipException.class)
    public ResponseEntity<ProblemDetail> handlePostOwnershipException(Exception ex) {
        return new ResponseEntity<>(
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

}
