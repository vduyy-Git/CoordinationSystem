package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.dtoresponse.ProfileResponse;
import com.coordinationsystem.dtorequest.ChangePasswordRequest;
import com.coordinationsystem.dtorequest.UpdateProfileRequest;
import com.coordinationsystem.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService service;
    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping
    public ApiResponse<ProfileResponse> get(Authentication auth) {
        return ApiResponse.success("Lấy hồ sơ thành công", service.get(auth.getName()));
    }

    @PutMapping
    public ApiResponse<ProfileResponse> update(Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Cập nhật hồ sơ thành công", service.update(auth.getName(), request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(auth.getName(), request);
        return ApiResponse.success("Đổi mật khẩu thành công");
    }
}
