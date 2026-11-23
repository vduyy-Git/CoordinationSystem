package com.coordinationsystem.service;

import com.coordinationsystem.model.User;
import com.coordinationsystem.model.UserRole;
import com.coordinationsystem.repository.RoleRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final String email;
    private final String password;
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(@Value("${app.bootstrap-admin.email:}") String email,
                          @Value("${app.bootstrap-admin.password:}") String password,
                          UserRepository users, RoleRepository roles, UserRoleRepository userRoles,
                          PasswordEncoder passwordEncoder) {
        this.email = email;
        this.password = password;
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Cần cấu hình APP_BOOTSTRAP_ADMIN_EMAIL và mật khẩu APP_BOOTSTRAP_ADMIN_PASSWORD tối thiểu 12 ký tự");
        }
        var existing = users.findByEmailIgnoreCase(email.trim());
        if (existing.isPresent()) {
            if (userRoles.findByUser_EmailIgnoreCaseAndRole_RoleName(email.trim(), "ADMIN").isEmpty()) {
                throw new IllegalStateException("Email bootstrap đã tồn tại nhưng chưa có quyền ADMIN; không tự nâng quyền tài khoản cũ");
            }
            return;
        }
        var role = roles.findByRoleNameIgnoreCase("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Chưa khởi tạo vai trò ADMIN trong cơ sở dữ liệu"));
        User admin = new User();
        admin.setFullName("System administrator");
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setStatus("ACTIVE");
        admin = users.save(admin);
        UserRole assignment = new UserRole();
        assignment.setUser(admin);
        assignment.setRole(role);
        userRoles.save(assignment);
    }
}
