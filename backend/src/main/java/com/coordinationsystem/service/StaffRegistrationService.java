package com.coordinationsystem.service;

import com.coordinationsystem.dtoresponse.StaffRegistrationResponse;
import com.coordinationsystem.dtorequest.StaffRegistrationRequest;
import com.coordinationsystem.exception.ConflictException;
import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Role;
import com.coordinationsystem.model.StaffRegistration;
import com.coordinationsystem.model.User;
import com.coordinationsystem.model.UserRole;
import com.coordinationsystem.repository.RoleRepository;
import com.coordinationsystem.repository.StaffRegistrationRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.UserRoleRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Service
@Transactional
public class StaffRegistrationService {
    private final StaffRegistrationRepository applications;
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder passwords;
    private final String registrationPin;
    public StaffRegistrationService(StaffRegistrationRepository applications, UserRepository users, RoleRepository roles,
                                    UserRoleRepository userRoles, PasswordEncoder passwords,
                                    @Value("${app.staff.registration-pin:}") String registrationPin) {
        this.applications = applications; this.users = users; this.roles = roles; this.userRoles = userRoles; this.passwords = passwords;
        this.registrationPin = registrationPin;
    }

    public StaffRegistrationResponse submit(StaffRegistrationRequest request) {
        if (registrationPin.isBlank() || !registrationPin.equals(request.authorizationPin()))
            throw new com.coordinationsystem.exception.ForBiddenException("Mã PIN đăng ký cán bộ không hợp lệ hoặc chưa được cấu hình");
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email) || applications.existsByEmailIgnoreCase(email))
            throw new ConflictException("Email đã được sử dụng hoặc đang chờ duyệt");
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new com.coordinationsystem.exception.BadRequestException("Mật khẩu không được vượt quá 72 byte UTF-8");
        StaffRegistration application = new StaffRegistration();
        application.setFullName(request.fullName().trim()); application.setEmail(email); application.setPhone(request.phone());
        application.setDepartment(request.department().trim()); application.setPasswordHash(passwords.encode(request.password()));
        return view(applications.save(application));
    }

    @Transactional(readOnly = true)
    public List<StaffRegistrationResponse> all() { return applications.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList(); }

    public StaffRegistrationResponse decide(Long id, String reviewerEmail, boolean approve) {
        StaffRegistration application = applications.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ đăng ký"));
        if (!"PENDING".equals(application.getStatus())) throw new ConflictException("Hồ sơ đã được xử lý");
        User reviewer = users.findByEmailIgnoreCase(reviewerEmail).orElseThrow(() -> new NotFoundException("Không tìm thấy người duyệt"));
        if (approve) {
            if (users.existsByEmailIgnoreCase(application.getEmail())) throw new ConflictException("Email đã được tạo tài khoản");
            User user = new User(); user.setFullName(application.getFullName()); user.setEmail(application.getEmail());
            user.setPhone(application.getPhone()); user.setPassword(application.getPasswordHash()); user.setStatus("ACTIVE");
            user = users.save(user);
            Role role = roles.findByRoleNameIgnoreCase("COORDINATOR").orElseThrow(() -> new NotFoundException("Chưa khởi tạo role COORDINATOR"));
            UserRole assignment = new UserRole(); assignment.setUser(user); assignment.setRole(role); assignment.setAssignedBy(reviewer);
            userRoles.save(assignment);
            application.setStatus("APPROVED");
        } else application.setStatus("REJECTED");
        application.setReviewedBy(reviewer); application.setReviewedAt(LocalDateTime.now());
        return view(applications.save(application));
    }

    private StaffRegistrationResponse view(StaffRegistration a) {
        return new StaffRegistrationResponse(a.getId(), a.getFullName(), a.getEmail(), a.getPhone(), a.getDepartment(),
                a.getStatus(), a.getCreatedAt(), a.getReviewedAt());
    }
}
