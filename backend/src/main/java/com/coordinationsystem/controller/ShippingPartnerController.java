package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.model.ShippingPartner;
import com.coordinationsystem.service.ShippingPartnerService;
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
@RequestMapping("/api/shipping-partners")
public class ShippingPartnerController {
    private final ShippingPartnerService service;

    public ShippingPartnerController(ShippingPartnerService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ShippingPartner>> getAll() {
        return ApiResponse.success("Lấy danh sách thành công", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ShippingPartner> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy dữ liệu thành công", service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShippingPartner>> create(@RequestBody ShippingPartner entity) {
        ShippingPartner created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dữ liệu thành công", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShippingPartner> update(@PathVariable Long id, @RequestBody ShippingPartner entity) {
        return ApiResponse.success("Cập nhật dữ liệu thành công", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Xóa dữ liệu thành công");
    }
}
