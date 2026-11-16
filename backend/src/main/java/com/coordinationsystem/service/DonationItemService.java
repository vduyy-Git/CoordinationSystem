package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.DonationItem;
import com.coordinationsystem.repository.DonationItemRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DonationItemService {
    private final DonationItemRepository repository;

    public DonationItemService(DonationItemRepository repository) {
        this.repository = repository;
    }

    public List<DonationItem> getAll() {
        return repository.findAll();
    }

    public DonationItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public DonationItem create(DonationItem entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public DonationItem update(Long id, DonationItem entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(DonationItem entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
