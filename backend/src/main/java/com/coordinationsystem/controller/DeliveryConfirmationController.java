package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.model.DeliveryConfirmation;
import com.coordinationsystem.dtorequest.ConfirmDeliveryRequest;
import com.coordinationsystem.service.DeliveryConfirmationService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-confirmations")
public class DeliveryConfirmationController {
    private final DeliveryConfirmationService service;

    public DeliveryConfirmationController(DeliveryConfirmationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<DeliveryConfirmation>> getAll() {
        return ApiResponse.success("Lấy danh sách thành công", service.getAll());
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<DeliveryConfirmation>> getMine(Authentication authentication) {
        return ApiResponse.success("Lấy xác nhận giao hàng của tôi thành công", service.getMine(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeliveryConfirmation> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy dữ liệu thành công", service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryConfirmation>> create(@Valid @RequestBody ConfirmDeliveryRequest command, Authentication authentication) {
        DeliveryConfirmation created = service.createForRecipient(command, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dữ liệu thành công", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<DeliveryConfirmation> update(@PathVariable Long id, @RequestBody DeliveryConfirmation entity) {
        return ApiResponse.success("Cập nhật dữ liệu thành công", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Xóa dữ liệu thành công");
    }
}
