package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Unit;
import com.coordinationsystem.repository.UnitRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UnitService {
    private final UnitRepository repository;

    public UnitService(UnitRepository repository) {
        this.repository = repository;
    }

    public List<Unit> getAll() {
        return repository.findAll();
    }

    public Unit getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Unit create(Unit entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public Unit update(Long id, Unit entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(Unit entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
