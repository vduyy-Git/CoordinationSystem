package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Allocation;
import com.coordinationsystem.repository.AllocationRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AllocationService {
    private final AllocationRepository repository;

    public AllocationService(AllocationRepository repository) {
        this.repository = repository;
    }

    public List<Allocation> getAll() {
        return repository.findAll();
    }

    public Allocation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Allocation create(Allocation entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public Allocation update(Long id, Allocation entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(Allocation entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
