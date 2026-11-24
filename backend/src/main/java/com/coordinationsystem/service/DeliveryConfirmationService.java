package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.DeliveryConfirmation;
import com.coordinationsystem.repository.DeliveryConfirmationRepository;
import com.coordinationsystem.repository.ShipmentItemRepository;
import com.coordinationsystem.repository.ShipmentRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.dtorequest.ConfirmDeliveryRequest;
import com.coordinationsystem.exception.ConflictException;
import com.coordinationsystem.exception.BadRequestException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeliveryConfirmationService {
    private final DeliveryConfirmationRepository repository;
    private final ShipmentItemRepository shipmentItems;
    private final ShipmentRepository shipments;
    private final UserRepository users;

    public DeliveryConfirmationService(DeliveryConfirmationRepository repository, ShipmentItemRepository shipmentItems,
                                       ShipmentRepository shipments, UserRepository users) {
        this.repository = repository;
        this.shipmentItems = shipmentItems;
        this.shipments = shipments;
        this.users = users;
    }

    public List<DeliveryConfirmation> getAll() {
        return repository.findAll();
    }

    public DeliveryConfirmation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public DeliveryConfirmation create(DeliveryConfirmation entity) {
        throw new BadRequestException("Xác nhận cần xác minh người nhận đang đăng nhập");
    }

    public DeliveryConfirmation createForRecipient(ConfirmDeliveryRequest command, String email) {
        var shipment = shipments.findById(command.shipmentId())
                .orElseThrow(() -> new com.coordinationsystem.exception.NotFoundException("Không tìm thấy shipment"));
        var recipient = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new com.coordinationsystem.exception.NotFoundException("Không tìm thấy người nhận"));
        var lines = shipmentItems.findByShipment_Id(command.shipmentId());
        if (lines.isEmpty() || !"DELIVERED".equals(shipment.getStatus())) {
            throw new ConflictException("Chỉ có thể xác nhận shipment đã giao thành công");
        }
        Long expectedRecipient = lines.get(0).getAllocationItem().getRequestItem().getRequest().getRecipient().getId();
        if (!expectedRecipient.equals(recipient.getId())) {
            throw new com.coordinationsystem.exception.ForBiddenException("Bạn không phải người nhận của shipment này");
        }
        DeliveryConfirmation confirmation = new DeliveryConfirmation();
        confirmation.setShipment(shipment);
        confirmation.setRecipient(recipient);
        confirmation.setReceivedBy(command.receivedBy().trim());
        confirmation.setConfirmationCode(command.confirmationCode());
        confirmation.setImageUrl(command.imageUrl());
        confirmation.setNote(command.note());
        confirmation.setDeliveryCondition(command.deliveryCondition());
        confirmation.setRating(command.rating());
        return repository.save(confirmation);
    }

    public List<DeliveryConfirmation> getMine(String email) {
        return repository.findByRecipient_EmailIgnoreCaseOrderByConfirmedAtDesc(email);
    }

    public DeliveryConfirmation update(Long id, DeliveryConfirmation entity) {
        throw new BadRequestException("Xác nhận giao hàng là bản ghi không thể sửa");
    }

    public void delete(Long id) {
        throw new BadRequestException("Xác nhận giao hàng là bản ghi không thể xóa");
    }

    private void setEntityId(DeliveryConfirmation entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
