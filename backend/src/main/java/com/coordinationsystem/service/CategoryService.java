package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Category;
import com.coordinationsystem.repository.CategoryRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> getAll() {
        return repository.findAll();
    }

    public Category getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Category create(Category entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public Category update(Long id, Category entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(Category entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
