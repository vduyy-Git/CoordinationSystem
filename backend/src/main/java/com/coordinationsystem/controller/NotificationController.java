package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.dtoresponse.NotificationResponse;
import com.coordinationsystem.repository.AssistanceRequestRepository;
import com.coordinationsystem.repository.DeliveryConfirmationRepository;
import com.coordinationsystem.repository.DonationRepository;
import com.coordinationsystem.repository.ShipmentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {
    private final AssistanceRequestRepository requests;
    private final DonationRepository donations;
    private final ShipmentRepository shipments;
    private final DeliveryConfirmationRepository confirmations;
    public NotificationController(AssistanceRequestRepository requests, DonationRepository donations,
                                  ShipmentRepository shipments, DeliveryConfirmationRepository confirmations) {
        this.requests = requests; this.donations = donations; this.shipments = shipments; this.confirmations = confirmations;
    }

    @GetMapping("/api/notifications/mine")
    public ApiResponse<List<NotificationResponse>> mine(Authentication auth) {
        List<NotificationResponse> result = new ArrayList<>();
        requests.findByRecipient_EmailIgnoreCaseOrderByCreatedAtDesc(auth.getName()).stream()
                .filter(r -> !"PENDING".equals(r.getStatus()))
                .forEach(r -> result.add(new NotificationResponse("request-" + r.getId(), "REQUEST",
                        "Cập nhật yêu cầu " + r.getRequestCode(), "Trạng thái hiện tại: " + r.getStatus(), r.getApprovedAt(), false)));
        donations.findByDonor_EmailIgnoreCaseOrderByCreatedAtDesc(auth.getName()).stream()
                .filter(d -> !"PENDING".equals(d.getStatus()))
                .forEach(d -> result.add(new NotificationResponse("donation-" + d.getId(), "DONATION",
                        "Cập nhật quyên góp " + d.getDonationCode(), "Trạng thái hiện tại: " + d.getStatus(), d.getApprovedAt(), false)));
        shipments.findForRecipient(auth.getName()).stream()
                .filter(s -> "DELIVERED".equals(s.getStatus()) && !confirmations.existsByShipment_Id(s.getId()))
                .forEach(s -> result.add(new NotificationResponse("shipment-" + s.getId(), "DELIVERY_CONFIRMATION",
                        "Xác nhận đã nhận hàng", "Chuyến giao " + s.getShipmentCode() + " đã được giao. Vui lòng xác nhận và đánh giá.", s.getDeliveredAt(), false)));
        result.sort(Comparator.comparing(NotificationResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return ApiResponse.success("Thông báo của tôi", result);
    }
}
