package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.model.AssistanceRequest;
import com.coordinationsystem.dtorequest.CreateAssistanceRequest;
import com.coordinationsystem.service.AssistanceRequestService;
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
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/assistance-requests")
public class AssistanceRequestController {
    private final AssistanceRequestService service;

    public AssistanceRequestController(AssistanceRequestService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AssistanceRequest>> getAll() {
        return ApiResponse.success("Lấy danh sách thành công", service.getAll());
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AssistanceRequest>> getMine(Authentication authentication) {
        return ApiResponse.success("Lấy danh sách yêu cầu của tôi thành công", service.getMine(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssistanceRequest> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy dữ liệu thành công", service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AssistanceRequest>> create(@Valid @RequestBody CreateAssistanceRequest command, Authentication authentication) {
        AssistanceRequest created = service.createForRecipient(command, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dữ liệu thành công", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<AssistanceRequest> update(@PathVariable Long id, @RequestBody AssistanceRequest entity) {
        return ApiResponse.success("Cập nhật dữ liệu thành công", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Xóa dữ liệu thành công");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ApiResponse<AssistanceRequest> approve(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Đã duyệt yêu cầu hỗ trợ", service.decide(id, authentication.getName(), true));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ApiResponse<AssistanceRequest> reject(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Đã từ chối yêu cầu hỗ trợ", service.decide(id, authentication.getName(), false));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<AssistanceRequest> cancel(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Đã hủy yêu cầu hỗ trợ", service.cancelByRecipient(id, authentication.getName()));
    }
}
