package com.backend.common.exception;

import com.backend.common.exception.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> errorHandler(CustomException e) {

        log.error("Error occurs {}", e.toString());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(Response.error(e.getErrorCode().name()));

    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> badCredentialsHandler(BadCredentialsException e) {
        log.error("Error occurs {}", e.toString());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Response.error(e.getMessage()));
    }

}
