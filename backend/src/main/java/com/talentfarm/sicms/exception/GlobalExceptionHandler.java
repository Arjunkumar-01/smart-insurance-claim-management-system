package com.talentfarm.sicms.exception;

import com.talentfarm.sicms.common.api.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import jakarta.servlet.http.HttpServletRequest;
import com.talentfarm.sicms.storage.exception.StorageException;
import com.talentfarm.sicms.storage.exception.StoredFileNotFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthenticationFailure(AuthenticationException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(Instant.now(), 401, "Unauthorized", "Invalid email or password", request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(Instant.now(), 409, "Conflict", exception.getMessage(), request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(Instant.now(), 404, "Not Found", exception.getMessage(), request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(InvalidConfigurationException.class)
    ResponseEntity<ApiError> handleConfiguration(InvalidConfigurationException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(Instant.now(), 500, "Internal Server Error", "Authentication configuration is unavailable", request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var fieldErrors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "Bad Request", "Request validation failed", request.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleInvalidArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(Instant.now(), 400, "Bad Request", exception.getMessage(), request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(StorageException.class)
    ResponseEntity<ApiError> handleStorage(StorageException exception, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "Bad Request", exception.getMessage(), request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(StoredFileNotFoundException.class)
    ResponseEntity<ApiError> handleStoredFileNotFound(StoredFileNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(Instant.now(), 404, "Not Found", exception.getMessage(), request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(Instant.now(), 409, "Conflict", "The resource was changed by another request", request.getRequestURI(), Map.of()));
    }
}