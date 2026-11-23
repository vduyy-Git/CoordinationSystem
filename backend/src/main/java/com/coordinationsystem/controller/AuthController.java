package com.coordinationsystem.controller;

import com.coordinationsystem.dtorequest.RegisterRequest;
import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.dtoresponse.AuthResponse;
import com.coordinationsystem.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tài khoản thành công", service.register(request)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Đăng nhập thành công", service.login(request.email(), request.password()));
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse> me(Authentication authentication) {
        return ApiResponse.success("Lấy tài khoản thành công", service.currentUser(authentication.getName()));
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }
}
