package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.model.Shipment;
import com.coordinationsystem.model.ShipmentEvent;
import com.coordinationsystem.dtorequest.ShipmentStatusRequest;
import com.coordinationsystem.dtorequest.CreateShipmentRequest;
import com.coordinationsystem.service.ShipmentService;
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
@RequestMapping("/api/shipments")
public class ShipmentController {
    private final ShipmentService service;

    public ShipmentController(ShipmentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Shipment>> getAll() {
        return ApiResponse.success("Lấy danh sách thành công", service.getAll());
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Shipment>> getMine(Authentication authentication) {
        return ApiResponse.success("Chuyến giao của tôi", service.getMine(authentication.getName()));
    }

    @GetMapping("/{id}/events")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ShipmentEvent>> getMyEvents(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Lịch sử chuyến giao", service.getMyEvents(id, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Shipment> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy dữ liệu thành công", service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Shipment>> create(@RequestBody Shipment entity) {
        Shipment created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dữ liệu thành công", created));
    }

    @PostMapping("/from-allocation")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ApiResponse<Shipment>> createForAllocation(@Valid @RequestBody CreateShipmentRequest request) {
        Shipment created = service.createForAllocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Đã tạo chuyến giao", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<Shipment> update(@PathVariable Long id, @RequestBody Shipment entity) {
        return ApiResponse.success("Cập nhật dữ liệu thành công", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Xóa dữ liệu thành công");
    }

    @PostMapping("/{id}/events")
    public ApiResponse<Shipment> changeStatus(@PathVariable Long id, @Valid @RequestBody ShipmentStatusRequest request,
                                               Authentication authentication) {
        return ApiResponse.success("Đã cập nhật trạng thái vận chuyển",
                service.changeStatus(id, request, authentication.getName()));
    }
}
