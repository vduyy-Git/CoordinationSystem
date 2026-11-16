package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.SupplyMovement;
import com.coordinationsystem.repository.SupplyMovementRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SupplyMovementService {
    private final SupplyMovementRepository repository;

    public SupplyMovementService(SupplyMovementRepository repository) {
        this.repository = repository;
    }

    public List<SupplyMovement> getAll() {
        return repository.findAll();
    }

    public SupplyMovement getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public SupplyMovement create(SupplyMovement entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public SupplyMovement update(Long id, SupplyMovement entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(SupplyMovement entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
