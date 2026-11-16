package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Shipment;
import com.coordinationsystem.repository.ShipmentRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShipmentService {
    private final ShipmentRepository repository;

    public ShipmentService(ShipmentRepository repository) {
        this.repository = repository;
    }

    public List<Shipment> getAll() {
        return repository.findAll();
    }

    public Shipment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Shipment create(Shipment entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public Shipment update(Long id, Shipment entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(Shipment entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
