package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.ShipmentEvent;
import com.coordinationsystem.repository.ShipmentEventRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShipmentEventService {
    private final ShipmentEventRepository repository;

    public ShipmentEventService(ShipmentEventRepository repository) {
        this.repository = repository;
    }

    public List<ShipmentEvent> getAll() {
        return repository.findAll();
    }

    public ShipmentEvent getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public ShipmentEvent create(ShipmentEvent entity) {
        throw new com.coordinationsystem.exception.BadRequestException("Tạo sự kiện qua POST /api/shipments/{id}/events");
    }

    public ShipmentEvent update(Long id, ShipmentEvent entity) {
        throw new com.coordinationsystem.exception.BadRequestException("Lịch sử shipment không thể sửa trực tiếp");
    }

    public void delete(Long id) {
        throw new com.coordinationsystem.exception.BadRequestException("Lịch sử shipment không thể xóa trực tiếp");
    }

    private void setEntityId(ShipmentEvent entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
