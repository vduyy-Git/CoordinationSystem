package com.coordinationsystem.service;

import com.coordinationsystem.dtorequest.RegisterRequest;
import com.coordinationsystem.dtoresponse.AuthResponse;
import com.coordinationsystem.exception.ConflictException;
import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.exception.UnAuthorizeException;
import com.coordinationsystem.model.User;
import com.coordinationsystem.model.UserRole;
import com.coordinationsystem.repository.RoleRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.UserRoleRepository;
import com.coordinationsystem.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.security.MessageDigest;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, RoleRepository roles, UserRoleRepository userRoles,
                       PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new com.coordinationsystem.exception.BadRequestException("Mật khẩu không được vượt quá 72 byte UTF-8");
        }
        if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("Email đã được sử dụng");
        var role = roles.findByRoleNameIgnoreCase("USER")
                .orElseThrow(() -> new NotFoundException("Chưa khởi tạo vai trò USER trong cơ sở dữ liệu"));
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setStatus("ACTIVE");
        user = users.save(user);
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoles.save(userRole);
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        User user = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UnAuthorizeException("Email hoặc mật khẩu không đúng"));
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new UnAuthorizeException("Email hoặc mật khẩu không đúng");
        }
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches && !user.getPassword().startsWith("$2")
                && MessageDigest.isEqual(user.getPassword().getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8))) {
            user.setPassword(passwordEncoder.encode(password));
            users.save(user);
            matches = true;
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus()) || !matches) {
            throw new UnAuthorizeException("Email hoặc mật khẩu không đúng");
        }
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse currentUser(String email) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        return response(user);
    }

    private AuthResponse response(User user) {
        var roleNames = userRoles.findByUser_Id(user.getId()).stream()
                .map(ur -> ur.getRole().getRoleName()).toList();
        return new AuthResponse(tokens.create(user.getEmail()), "Bearer", tokens.expiresInSeconds(),
                user.getId(), user.getFullName(), user.getEmail(), roleNames);
    }
}
