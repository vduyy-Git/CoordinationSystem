package com.coordinationsystem.controller;

import com.coordinationsystem.exception.BadRequestException;
import com.coordinationsystem.repository.AssistanceRequestRepository;
import com.coordinationsystem.repository.AllocationRepository;
import com.coordinationsystem.repository.DonationRepository;
import com.coordinationsystem.repository.ShipmentRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.service.InventoryService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportExportController {
    private final AssistanceRequestRepository requests;
    private final DonationRepository donations;
    private final ShipmentRepository shipments;
    private final AllocationRepository allocations;
    private final UserRepository users;
    private final InventoryService inventory;
    public ReportExportController(AssistanceRequestRepository requests, DonationRepository donations,
                                  ShipmentRepository shipments, AllocationRepository allocations,
                                  UserRepository users, InventoryService inventory) {
        this.requests = requests; this.donations = donations; this.shipments = shipments;
        this.allocations = allocations; this.users = users; this.inventory = inventory;
    }

    @GetMapping("/api/reports/export")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<byte[]> export(@RequestParam String dataset, @RequestParam(defaultValue = "csv") String format) {
        if (!"csv".equalsIgnoreCase(format)) throw new BadRequestException("Hiện chỉ hỗ trợ xuất CSV (mở được bằng Excel)");
        List<String> rows = switch (dataset.toLowerCase()) {
            case "requests" -> {
                var result = new java.util.ArrayList<String>();
                result.add("requestCode,title,priority,status,deliveryAddress,createdAt");
                requests.findAll().forEach(r -> result.add(csv(r.getRequestCode(), r.getTitle(), r.getPriority(), r.getStatus(), r.getDeliveryAddress(), String.valueOf(r.getCreatedAt()))));
                yield result;
            }
            case "donations" -> {
                var result = new java.util.ArrayList<String>();
                result.add("donationCode,donorEmail,type,status,pickupAddress,createdAt");
                donations.findAll().forEach(d -> result.add(csv(d.getDonationCode(), d.getDonor().getEmail(), d.getDonationType(), d.getStatus(), d.getPickupAddress(), String.valueOf(d.getCreatedAt()))));
                yield result;
            }
            case "shipments" -> {
                var result = new java.util.ArrayList<String>();
                result.add("shipmentCode,status,trackingCode,pickupAddress,deliveryAddress,createdAt");
                shipments.findAll().forEach(s -> result.add(csv(s.getShipmentCode(), s.getStatus(), s.getTrackingCode(), s.getPickupAddress(), s.getDeliveryAddress(), String.valueOf(s.getCreatedAt()))));
                yield result;
            }
            case "allocations" -> {
                var result = new java.util.ArrayList<String>();
                result.add("allocationCode,requestCode,recipientEmail,status,allocatedAt,note");
                allocations.findAll().forEach(a -> result.add(csv(a.getAllocationCode(), a.getRequest().getRequestCode(),
                        a.getRequest().getRecipient().getEmail(), a.getStatus(), String.valueOf(a.getAllocatedAt()), a.getNote())));
                yield result;
            }
            case "beneficiaries" -> {
                var result = new java.util.ArrayList<String>();
                result.add("fullName,email,phone,citizenId,beneficiaryGroup,ward,address,status");
                users.findAll().stream().filter(u -> u.getCitizenId() != null || u.getBeneficiaryGroup() != null)
                        .forEach(u -> result.add(csv(u.getFullName(), u.getEmail(), u.getPhone(), u.getCitizenId(),
                                u.getBeneficiaryGroup(), u.getWard(), u.getAddress(), u.getStatus())));
                yield result;
            }
            case "summary" -> {
                var result = new java.util.ArrayList<String>();
                result.add("metric,value");
                result.add(csv("requests", String.valueOf(requests.count())));
                result.add(csv("donations", String.valueOf(donations.count())));
                result.add(csv("allocations", String.valueOf(allocations.count())));
                result.add(csv("shipments", String.valueOf(shipments.count())));
                result.add(csv("beneficiaries", String.valueOf(users.findAll().stream()
                        .filter(u -> u.getCitizenId() != null || u.getBeneficiaryGroup() != null).count())));
                result.add(csv("inventoryItems", String.valueOf(inventory.summary().size())));
                yield result;
            }
            case "inventory" -> {
                var result = new java.util.ArrayList<String>();
                result.add("itemId,itemName,category,unit,accepted,reserved,delivered,available");
                inventory.summary().forEach(i -> result.add(csv(String.valueOf(i.itemId()), i.itemName(), i.category(), i.unit(),
                        String.valueOf(i.accepted()), String.valueOf(i.reserved()), String.valueOf(i.delivered()), String.valueOf(i.available()))));
                yield result;
            }
            default -> throw new BadRequestException("dataset phải là requests, donations, shipments hoặc inventory");
        };
        byte[] bytes = ("\uFEFF" + String.join("\r\n", rows) + "\r\n").getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=coordination-" + dataset.toLowerCase() + ".csv")
                .body(bytes);
    }

    private String csv(String... values) {
        return java.util.Arrays.stream(values).map(value -> "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"")
                .collect(java.util.stream.Collectors.joining(","));
    }
}
