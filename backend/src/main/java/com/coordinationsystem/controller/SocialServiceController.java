package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.dtorequest.CreateServiceRegistrationRequest;
import com.coordinationsystem.model.ServiceRegistration;
import com.coordinationsystem.model.SocialService;
import com.coordinationsystem.service.SocialServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class SocialServiceController {
    private final SocialServiceService service;
    public SocialServiceController(SocialServiceService service) { this.service = service; }

    @GetMapping("/api/services")
    public ApiResponse<List<SocialService>> catalog() { return ApiResponse.success("Danh sách dịch vụ", service.catalog()); }

    @GetMapping("/api/admin/services")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ApiResponse<List<SocialService>> all() { return ApiResponse.success("Danh sách dịch vụ", service.all()); }

    @PostMapping("/api/admin/services")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SocialService>> create(@Valid @RequestBody SocialService value) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Đã tạo dịch vụ", service.create(value)));
    }

    @PutMapping("/api/admin/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SocialService> update(@PathVariable Long id, @Valid @RequestBody SocialService value) {
        return ApiResponse.success("Đã cập nhật dịch vụ", service.update(id, value));
    }

    @DeleteMapping("/api/admin/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) { service.delete(id); return ApiResponse.success("Đã xóa dịch vụ"); }

    @PostMapping("/api/service-registrations")
    public ResponseEntity<ApiResponse<ServiceRegistration>> register(Authentication auth, @Valid @RequestBody CreateServiceRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Đã gửi đăng ký", service.register(request, auth.getName())));
    }

    @GetMapping("/api/service-registrations/mine")
    public ApiResponse<List<ServiceRegistration>> mine(Authentication auth) { return ApiResponse.success("Đăng ký của tôi", service.mine(auth.getName())); }

    @GetMapping("/api/admin/service-registrations")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ApiResponse<List<ServiceRegistration>> allRegistrations() { return ApiResponse.success("Danh sách đăng ký", service.allRegistrations()); }

    @PutMapping("/api/admin/service-registrations/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ApiResponse<ServiceRegistration> decide(@PathVariable Long id, @RequestParam String value) {
        if (!List.of("APPROVED", "REJECTED", "COMPLETED").contains(value))
            throw new com.coordinationsystem.exception.BadRequestException("Trạng thái không hợp lệ");
        return ApiResponse.success("Đã cập nhật đăng ký", service.decide(id, value));
    }
}
