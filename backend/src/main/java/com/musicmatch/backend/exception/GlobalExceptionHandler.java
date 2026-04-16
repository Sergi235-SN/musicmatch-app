package com.musicmatch.backend.exception;

import com.musicmatch.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(UserBlockedException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
}