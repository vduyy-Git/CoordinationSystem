package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.AllocationItem;
import com.coordinationsystem.repository.AllocationItemRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AllocationItemService {
    private final AllocationItemRepository repository;

    public AllocationItemService(AllocationItemRepository repository) {
        this.repository = repository;
    }

    public List<AllocationItem> getAll() {
        return repository.findAll();
    }

    public AllocationItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public AllocationItem create(AllocationItem entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public AllocationItem update(Long id, AllocationItem entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(AllocationItem entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
