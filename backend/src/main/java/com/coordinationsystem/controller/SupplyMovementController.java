package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.model.SupplyMovement;
import com.coordinationsystem.service.SupplyMovementService;
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

import java.util.List;

@RestController
@RequestMapping("/api/supply-movements")
public class SupplyMovementController {
    private final SupplyMovementService service;

    public SupplyMovementController(SupplyMovementService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SupplyMovement>> getAll() {
        return ApiResponse.success("Lấy danh sách thành công", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplyMovement> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy dữ liệu thành công", service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplyMovement>> create(@RequestBody SupplyMovement entity) {
        SupplyMovement created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dữ liệu thành công", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplyMovement> update(@PathVariable Long id, @RequestBody SupplyMovement entity) {
        return ApiResponse.success("Cập nhật dữ liệu thành công", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Xóa dữ liệu thành công");
    }
}
