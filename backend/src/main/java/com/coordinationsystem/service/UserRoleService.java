package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.UserRole;
import com.coordinationsystem.repository.UserRoleRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserRoleService {
    private final UserRoleRepository repository;

    public UserRoleService(UserRoleRepository repository) {
        this.repository = repository;
    }

    public List<UserRole> getAll() {
        return repository.findAll();
    }

    public UserRole getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public UserRole create(UserRole entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public UserRole update(Long id, UserRole entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(UserRole entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
