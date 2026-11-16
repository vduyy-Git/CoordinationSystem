package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.ShippingPartner;
import com.coordinationsystem.repository.ShippingPartnerRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShippingPartnerService {
    private final ShippingPartnerRepository repository;

    public ShippingPartnerService(ShippingPartnerRepository repository) {
        this.repository = repository;
    }

    public List<ShippingPartner> getAll() {
        return repository.findAll();
    }

    public ShippingPartner getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public ShippingPartner create(ShippingPartner entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public ShippingPartner update(Long id, ShippingPartner entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(ShippingPartner entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
