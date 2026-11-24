package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.dtoresponse.InventorySummaryResponse;
import com.coordinationsystem.service.InventoryService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {
    private final InventoryService service;
    public InventoryController(InventoryService service) { this.service = service; }
    @GetMapping("/api/inventory/summary")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ApiResponse<List<InventorySummaryResponse>> summary() {
        return ApiResponse.success("Tồn kho khả dụng", service.summary());
    }
}
