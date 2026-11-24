package com.coordinationsystem.controller;

import com.coordinationsystem.dtoresponse.ApiResponse;
import com.coordinationsystem.dtorequest.ConfirmDeliveryRequest;
import com.coordinationsystem.dtorequest.CreateFeedbackRequest;
import com.coordinationsystem.model.DeliveryConfirmation;
import com.coordinationsystem.service.DeliveryConfirmationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final DeliveryConfirmationService service;
    public FeedbackController(DeliveryConfirmationService service) { this.service = service; }

    @GetMapping("/mine")
    public ApiResponse<List<DeliveryConfirmation>> mine(Authentication auth) {
        return ApiResponse.success("Phản hồi của tôi", service.getMine(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryConfirmation>> submit(Authentication auth, @Valid @RequestBody CreateFeedbackRequest request) {
        var command = new ConfirmDeliveryRequest(request.shipmentId(), request.receivedBy(), request.confirmationCode(),
                request.imageUrl(), request.comment(), request.deliveryCondition(), request.rating());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Đã ghi nhận xác nhận và phản hồi", service.createForRecipient(command, auth.getName())));
    }
}
