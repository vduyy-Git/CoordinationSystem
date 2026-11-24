package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.repository.*;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {
    private final AssistanceRequestRepository requests;
    private final DonationRepository donations;
    private final AllocationRepository allocations;
    private final ShipmentRepository shipments;
    private final DeliveryConfirmationRepository confirmations;
    private final UserRepository users;

    public DashboardController(AssistanceRequestRepository requests, DonationRepository donations,
                               AllocationRepository allocations, ShipmentRepository shipments,
                               DeliveryConfirmationRepository confirmations, UserRepository users) {
        this.requests = requests; this.donations = donations; this.allocations = allocations;
        this.shipments = shipments; this.confirmations = confirmations; this.users = users;
    }

    @GetMapping("/api/dashboard/summary")
    public ApiResponse<Map<String, Object>> summary(Authentication auth) {
        boolean coordinator = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_COORDINATOR"));
        var requestRows = coordinator ? requests.findAll() : requests.findByRecipient_EmailIgnoreCaseOrderByCreatedAtDesc(auth.getName());
        var donationRows = coordinator ? donations.findAll() : donations.findByDonor_EmailIgnoreCaseOrderByCreatedAtDesc(auth.getName());
        var shipmentRows = coordinator ? shipments.findAll() : shipments.findForRecipient(auth.getName());
        var data = Map.<String, Object>of(
                "scope", coordinator ? "COORDINATION" : "PERSONAL",
                "requests", requestRows.size(), "requestsByStatus", requestRows.stream().collect(Collectors.groupingBy(r -> r.getStatus(), Collectors.counting())),
                "donations", donationRows.size(), "donationsByStatus", donationRows.stream().collect(Collectors.groupingBy(d -> d.getStatus(), Collectors.counting())),
                "allocations", coordinator ? allocations.count() : 0L,
                "shipments", shipmentRows.size(), "shipmentsByStatus", shipmentRows.stream().collect(Collectors.groupingBy(s -> s.getStatus(), Collectors.counting())),
                "deliveryConfirmations", coordinator ? confirmations.count() : confirmations.findByRecipient_EmailIgnoreCaseOrderByConfirmedAtDesc(auth.getName()).size(),
                "beneficiaries", coordinator ? users.findAll().stream().filter(u -> u.getCitizenId() != null || u.getBeneficiaryGroup() != null).count() : 0L);
        return ApiResponse.success("Tổng quan hệ thống", data);
    }

    @GetMapping("/api/reports/summary")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ApiResponse<Map<String, Object>> report() {
        var requestRows = requests.findAll();
        var donationRows = donations.findAll();
        var shipmentRows = shipments.findAll();
        var data = Map.<String, Object>of(
                "requests", requestRows.size(), "requestsByStatus", requestRows.stream().collect(Collectors.groupingBy(r -> r.getStatus(), Collectors.counting())),
                "donations", donationRows.size(), "donationsByStatus", donationRows.stream().collect(Collectors.groupingBy(d -> d.getStatus(), Collectors.counting())),
                "allocations", allocations.count(), "shipments", shipmentRows.size(),
                "shipmentsByStatus", shipmentRows.stream().collect(Collectors.groupingBy(s -> s.getStatus(), Collectors.counting())),
                "deliveryConfirmations", confirmations.count(),
                "beneficiaries", users.findAll().stream().filter(u -> u.getCitizenId() != null || u.getBeneficiaryGroup() != null).count());
        return ApiResponse.success("Báo cáo tổng hợp", data);
    }
}
