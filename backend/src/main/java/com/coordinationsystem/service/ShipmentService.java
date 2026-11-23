package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Shipment;
import com.coordinationsystem.model.ShipmentEvent;
import com.coordinationsystem.repository.ShipmentRepository;
import com.coordinationsystem.repository.ShipmentEventRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.ShipmentItemRepository;
import com.coordinationsystem.repository.SupplyMovementRepository;
import com.coordinationsystem.repository.DonationItemRepository;
import com.coordinationsystem.repository.AssistanceRequestRepository;
import com.coordinationsystem.repository.RequestItemRepository;
import com.coordinationsystem.repository.AllocationItemRepository;
import com.coordinationsystem.repository.AllocationRepository;
import com.coordinationsystem.model.SupplyMovement;
import com.coordinationsystem.dtorequest.ShipmentStatusRequest;
import com.coordinationsystem.exception.BadRequestException;
import com.coordinationsystem.exception.ConflictException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@Transactional
public class ShipmentService {
    private final ShipmentRepository repository;
    private final ShipmentEventRepository events;
    private final UserRepository users;
    private final ShipmentItemRepository shipmentItems;
    private final DonationItemRepository donationItems;
    private final SupplyMovementRepository movements;
    private final AssistanceRequestRepository assistanceRequests;
    private final RequestItemRepository requestItems;
    private final AllocationItemRepository allocationItems;
    private final AllocationRepository allocations;

    public ShipmentService(ShipmentRepository repository, ShipmentEventRepository events, UserRepository users,
                           ShipmentItemRepository shipmentItems, DonationItemRepository donationItems,
                           SupplyMovementRepository movements, AssistanceRequestRepository assistanceRequests,
                           RequestItemRepository requestItems, AllocationItemRepository allocationItems,
                           AllocationRepository allocations) {
        this.repository = repository;
        this.events = events;
        this.users = users;
        this.shipmentItems = shipmentItems;
        this.donationItems = donationItems;
        this.movements = movements;
        this.assistanceRequests = assistanceRequests;
        this.requestItems = requestItems;
        this.allocationItems = allocationItems;
        this.allocations = allocations;
    }

    public List<Shipment> getAll() {
        return repository.findAll();
    }

    public Shipment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Shipment create(Shipment entity) {
        if (entity.getPickupAddress() == null || entity.getPickupAddress().isBlank()
                || entity.getDeliveryAddress() == null || entity.getDeliveryAddress().isBlank()) {
            throw new BadRequestException("Địa chỉ lấy hàng và giao hàng là bắt buộc");
        }
        entity.setShipmentCode("SH-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        entity.setStatus("PENDING");
        entity.setPickedUpAt(null);
        entity.setDeliveredAt(null);
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public Shipment update(Long id, Shipment entity) {
        Shipment current = getById(id);
        current.setPartner(entity.getPartner());
        current.setTrackingCode(entity.getTrackingCode());
        current.setTrackingUrl(entity.getTrackingUrl());
        return repository.save(current);
    }

    public void delete(Long id) {
        throw new BadRequestException("Không hỗ trợ xóa shipment; hãy chuyển trạng thái sang CANCELLED");
    }

    public Shipment changeStatus(Long id, ShipmentStatusRequest change, String actorEmail) {
        Shipment shipment = repository.findLockedById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
        var actor = users.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người cập nhật"));
        String next = change.status().trim().toUpperCase();
        Map<String, Set<String>> transitions = Map.of(
                "PENDING", Set.of("BOOKED", "CANCELLED"),
                "BOOKED", Set.of("PICKED_UP", "CANCELLED", "FAILED"),
                "PICKED_UP", Set.of("IN_TRANSIT", "FAILED"),
                "IN_TRANSIT", Set.of("OUT_FOR_DELIVERY", "FAILED", "RETURNED"),
                "OUT_FOR_DELIVERY", Set.of("DELIVERED", "FAILED", "RETURNED"),
                "FAILED", Set.of("BOOKED", "RETURNED"));
        if (!transitions.getOrDefault(shipment.getStatus(), Set.of()).contains(next)) {
            throw new ConflictException("Không thể chuyển shipment từ " + shipment.getStatus() + " sang " + next);
        }
        if ("BOOKED".equals(next) && shipmentItems.findByShipment_Id(id).stream()
                .anyMatch(item -> "CANCELLED".equals(item.getAllocationItem().getAllocation().getStatus()))) {
            throw new ConflictException("Không thể đặt lịch cho shipment thuộc phân bổ đã hủy");
        }
        shipment.setStatus(next);
        if ("DELIVERED".equals(next)) {
            var lines = shipmentItems.findByShipment_Id(id);
            if (lines.isEmpty()) throw new ConflictException("Không thể giao shipment chưa có vật tư");
            for (var line : lines) {
                var allocationItem = line.getAllocationItem();
                var donationItem = donationItems.findLockedById(allocationItem.getDonationItem().getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy vật tư tồn kho"));
                if (donationItem.getReservedQuantity().compareTo(line.getQuantity()) < 0) {
                    throw new ConflictException("Tồn kho đã giữ không đủ để xác nhận giao");
                }
                donationItem.setReservedQuantity(donationItem.getReservedQuantity().subtract(line.getQuantity()));
                donationItem.setDeliveredQuantity(donationItem.getDeliveredQuantity().add(line.getQuantity()));
                donationItems.save(donationItem);
                SupplyMovement movement = new SupplyMovement();
                movement.setDonationItem(donationItem);
                movement.setMovementType("DELIVERED");
                movement.setQuantity(line.getQuantity());
                movement.setFromLocationType("ALLOCATION");
                movement.setFromLocationId(allocationItem.getAllocation().getId());
                movement.setToLocationType("RECIPIENT");
                movement.setToLocationId(allocationItem.getRequestItem().getRequest().getRecipient().getId());
                movement.setReferenceType("SHIPMENT");
                movement.setReferenceId(id);
                movement.setCreatedBy(actor);
                movements.save(movement);
            }
            Long requestId = lines.get(0).getAllocationItem().getRequestItem().getRequest().getId();
            boolean complete = requestItems.findByRequest_Id(requestId).stream().allMatch(requestItem ->
                    shipmentItems.sumDeliveredForRequestItem(requestItem.getId())
                            .compareTo(requestItem.getApprovedQuantity()) >= 0);
            if (complete) {
                var assistanceRequest = assistanceRequests.findById(requestId)
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hỗ trợ"));
                assistanceRequest.setStatus("COMPLETED");
                assistanceRequests.save(assistanceRequest);
            }
            lines.stream().map(line -> line.getAllocationItem().getAllocation().getId()).distinct().forEach(allocationId -> {
                boolean allocationComplete = allocationItems.findByAllocation_Id(allocationId).stream().allMatch(item ->
                        shipmentItems.sumDeliveredForAllocationItem(item.getId()).compareTo(item.getQuantity()) >= 0);
                if (allocationComplete) {
                    allocations.findById(allocationId).ifPresent(allocation -> {
                        allocation.setStatus("COMPLETED");
                        allocations.save(allocation);
                    });
                }
            });
        }
        if ("PICKED_UP".equals(next)) shipment.setPickedUpAt(LocalDateTime.now());
        if ("DELIVERED".equals(next)) shipment.setDeliveredAt(LocalDateTime.now());
        repository.save(shipment);

        ShipmentEvent event = new ShipmentEvent();
        event.setShipment(shipment);
        event.setStatus(next);
        event.setDescription(change.description());
        event.setLocation(change.location());
        event.setUpdatedBy(actor);
        events.save(event);
        return shipment;
    }

    private void setEntityId(Shipment entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
