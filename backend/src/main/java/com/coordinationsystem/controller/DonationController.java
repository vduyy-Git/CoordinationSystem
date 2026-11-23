package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.model.Donation;
import com.coordinationsystem.dtorequest.CreateDonationRequest;
import com.coordinationsystem.service.DonationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationService service;

    public DonationController(DonationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Donation>> getAll() {
        return ApiResponse.success("Lấy danh sách thành công", service.getAll());
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Donation>> getMine(Authentication authentication) {
        return ApiResponse.success("Lấy danh sách quyên góp của tôi thành công", service.getMine(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Donation> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy dữ liệu thành công", service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Donation>> create(@Valid @RequestBody CreateDonationRequest command, Authentication authentication) {
        Donation created = service.createForDonor(command, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dữ liệu thành công", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<Donation> update(@PathVariable Long id, @RequestBody Donation entity) {
        return ApiResponse.success("Cập nhật dữ liệu thành công", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Xóa dữ liệu thành công");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ApiResponse<Donation> approve(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Đã duyệt quyên góp", service.decide(id, authentication.getName(), true));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ApiResponse<Donation> reject(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Đã từ chối quyên góp", service.decide(id, authentication.getName(), false));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Donation> cancel(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Đã hủy quyên góp", service.cancelByDonor(id, authentication.getName()));
    }
}
