package com.coordinationsystem.service;

import com.coordinationsystem.dtorequest.CreateServiceRegistrationRequest;
import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.ServiceRegistration;
import com.coordinationsystem.model.SocialService;
import com.coordinationsystem.repository.ServiceRegistrationRepository;
import com.coordinationsystem.repository.SocialServiceRepository;
import com.coordinationsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class SocialServiceService {
    private final SocialServiceRepository services;
    private final ServiceRegistrationRepository registrations;
    private final UserRepository users;

    public SocialServiceService(SocialServiceRepository services, ServiceRegistrationRepository registrations, UserRepository users) {
        this.services = services;
        this.registrations = registrations;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<SocialService> catalog() { return services.findByStatusIgnoreCaseOrderByNameAsc("ACTIVE"); }
    @Transactional(readOnly = true)
    public List<SocialService> all() { return services.findAll(); }
    public SocialService create(SocialService service) { service.setId(null); return services.save(service); }
    public SocialService update(Long id, SocialService value) {
        SocialService existing = services.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ"));
        existing.setName(value.getName()); existing.setDescription(value.getDescription());
        existing.setServiceType(value.getServiceType() == null ? existing.getServiceType() : value.getServiceType());
        existing.setServiceArea(value.getServiceArea());
        existing.setContactPhone(value.getContactPhone());
        existing.setStatus(value.getStatus() == null ? existing.getStatus() : value.getStatus());
        return services.save(existing);
    }
    public void delete(Long id) { services.delete(services.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ"))); }

    public ServiceRegistration register(CreateServiceRegistrationRequest request, String email) {
        var service = services.findById(request.serviceId()).orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ"));
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        ServiceRegistration registration = new ServiceRegistration();
        registration.setService(service); registration.setUser(user); registration.setRequestDetails(request.requestDetails());
        registration.setStatus("PENDING");
        return registrations.save(registration);
    }
    @Transactional(readOnly = true)
    public List<ServiceRegistration> mine(String email) { return registrations.findByUser_EmailIgnoreCaseOrderByCreatedAtDesc(email); }
    @Transactional(readOnly = true)
    public List<ServiceRegistration> allRegistrations() { return registrations.findAll(); }
    public ServiceRegistration decide(Long id, String status) {
        ServiceRegistration registration = registrations.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy đăng ký"));
        registration.setStatus(status);
        return registrations.save(registration);
    }
}
