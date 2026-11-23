/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.coordinationsystem.exception;

import com.coordinationsystem.dtoresponse.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;

/**
 *
 * @author Windows
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NotFoundException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, "NOT_FOUND", ex.getMessage(), request.getRequestURI(), List.of()));
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, "BAD_REQUEST", ex.getMessage(), request.getRequestURI(), List.of()));
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<?>> handleConflict(ConflictException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(409, "CONFLICT", ex.getMessage(), request.getRequestURI(), List.of()));
    }
    
    @ExceptionHandler(UnAuthorizeException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnAuthorizeException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "UNAUTHORIZED", ex.getMessage(), request.getRequestURI(), List.of()));
    }
    
    @ExceptionHandler(ForBiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbidden(ForBiddenException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(403, "FORBIDDEN", ex.getMessage(), request.getRequestURI(), List.of()));
    }
    
    //k hieu
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        String message = details.isEmpty() ? "Dữ liệu đầu vào không hợp lệ" : details.get(0);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "VALIDATION_ERROR", message, request.getRequestURI(), details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream().map(v -> v.getMessage()).toList();
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "VALIDATION_ERROR",
                details.isEmpty() ? "Dữ liệu đầu vào không hợp lệ" : details.get(0), request.getRequestURI(), details));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(409, "DATA_CONFLICT",
                "Dữ liệu đang bị ràng buộc bởi bản ghi khác hoặc khóa duy nhất", request.getRequestURI(), List.of()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobal(Exception ex){
        LOGGER.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal server error."));
    }
}
