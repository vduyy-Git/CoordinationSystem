package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.dtoresponse.StaffRegistrationResponse;
import com.coordinationsystem.dtorequest.StaffRegistrationRequest;
import com.coordinationsystem.service.StaffRegistrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class StaffRegistrationController {
    private final StaffRegistrationService service;
    public StaffRegistrationController(StaffRegistrationService service) { this.service = service; }

    @PostMapping("/api/auth/staff-registration")
    public ResponseEntity<ApiResponse<StaffRegistrationResponse>> submit(@Valid @RequestBody StaffRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Đã gửi hồ sơ cán bộ chờ phê duyệt", service.submit(request)));
    }

    @GetMapping("/api/admin/staff-registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<StaffRegistrationResponse>> all() { return ApiResponse.success("Hồ sơ cán bộ", service.all()); }

    @PostMapping("/api/admin/staff-registrations/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StaffRegistrationResponse> approve(@PathVariable Long id, Authentication auth) {
        return ApiResponse.success("Đã duyệt hồ sơ cán bộ", service.decide(id, auth.getName(), true));
    }

    @PostMapping("/api/admin/staff-registrations/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StaffRegistrationResponse> reject(@PathVariable Long id, Authentication auth) {
        return ApiResponse.success("Đã từ chối hồ sơ cán bộ", service.decide(id, auth.getName(), false));
    }
}
