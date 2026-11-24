package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.User;
import com.coordinationsystem.repository.UserRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.time.LocalDateTime;

import java.util.List;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public User create(User entity) {
        entity.setCitizenId(normalizeCitizenId(entity.getCitizenId()));
        if (entity.getPassword() == null || entity.getPassword().length() < 8) {
            throw new com.coordinationsystem.exception.BadRequestException("Mật khẩu phải có ít nhất 8 ký tự");
        }
        if (entity.getEmail() == null || entity.getFullName() == null || entity.getFullName().isBlank()) {
            throw new com.coordinationsystem.exception.BadRequestException("Họ tên và email là bắt buộc");
        }
        if (entity.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new com.coordinationsystem.exception.BadRequestException("Mật khẩu không được vượt quá 72 byte UTF-8");
        }
        entity.setEmail(entity.getEmail().trim().toLowerCase(Locale.ROOT));
        if (repository.existsByEmailIgnoreCase(entity.getEmail())) {
            throw new com.coordinationsystem.exception.ConflictException("Email đã được sử dụng");
        }
        if (entity.getCitizenId() != null && repository.existsByCitizenId(entity.getCitizenId())) {
            throw new com.coordinationsystem.exception.ConflictException("CCCD đã được sử dụng");
        }
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        entity.setStatus("ACTIVE");
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public User update(Long id, User entity) {
        getById(id);
        if (entity.getEmail() == null || entity.getFullName() == null || entity.getFullName().isBlank()) {
            throw new com.coordinationsystem.exception.BadRequestException("Họ tên và email là bắt buộc");
        }
        entity.setEmail(entity.getEmail().trim().toLowerCase(Locale.ROOT));
        User existing = getById(id);
        entity.setCitizenId(normalizeCitizenId(entity.getCitizenId()));
        if (entity.getStatus() == null) entity.setStatus(existing.getStatus());
        if (!existing.getEmail().equalsIgnoreCase(entity.getEmail()) && repository.existsByEmailIgnoreCase(entity.getEmail())) {
            throw new com.coordinationsystem.exception.ConflictException("Email đã được sử dụng");
        }
        if (entity.getCitizenId() != null && !entity.getCitizenId().equals(existing.getCitizenId())
                && repository.existsByCitizenId(entity.getCitizenId())) {
            throw new com.coordinationsystem.exception.ConflictException("CCCD đã được sử dụng");
        }
        if (entity.getPassword() != null && !entity.getPassword().isBlank()) {
            if (entity.getPassword().length() < 8) throw new com.coordinationsystem.exception.BadRequestException("Mật khẩu phải có ít nhất 8 ký tự");
            if (entity.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) throw new com.coordinationsystem.exception.BadRequestException("Mật khẩu không được vượt quá 72 byte UTF-8");
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        } else {
            entity.setPassword(existing.getPassword());
        }
        if (entity.getStatus() == null || !List.of("ACTIVE", "INACTIVE", "LOCKED").contains(entity.getStatus())) {
            throw new com.coordinationsystem.exception.BadRequestException("Trạng thái tài khoản không hợp lệ");
        }
        entity.setUpdatedAt(LocalDateTime.now());
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(User entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }

    private String normalizeCitizenId(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (!normalized.matches("[0-9]{12}")) {
            throw new com.coordinationsystem.exception.BadRequestException("CCCD phải gồm đúng 12 chữ số");
        }
        return normalized;
    }
}
