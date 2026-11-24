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
        String email;
        if (request.email() == null || request.email().isBlank()) {
            String identity = request.citizenId() == null || request.citizenId().isBlank()
                    ? request.phone() : request.citizenId();
            if (identity == null || identity.isBlank())
                throw new com.coordinationsystem.exception.BadRequestException("Cần email hoặc số điện thoại/CCCD để đăng ký");
            String safeIdentity = identity.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
            email = "citizen-" + safeIdentity + "@coordination.local";
        } else email = request.email().trim().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new com.coordinationsystem.exception.BadRequestException("Mật khẩu không được vượt quá 72 byte UTF-8");
        }
        if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("Email đã được sử dụng");
        if (request.phone() != null && !request.phone().isBlank() && users.existsByPhone(request.phone().trim()))
            throw new ConflictException("Số điện thoại đã được sử dụng");
        if (request.citizenId() != null && !request.citizenId().isBlank() && users.existsByCitizenId(request.citizenId().trim()))
            throw new ConflictException("CCCD đã được sử dụng");
        var role = roles.findByRoleNameIgnoreCase("USER")
                .orElseThrow(() -> new NotFoundException("Chưa khởi tạo vai trò USER trong cơ sở dữ liệu"));
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        user.setCitizenId(request.citizenId() == null || request.citizenId().isBlank() ? null : request.citizenId().trim());
        user.setBirthDate(request.birthDate());
        user.setAddress(request.address());
        user.setWard(request.ward());
        user.setBeneficiaryGroup(request.beneficiaryGroup());
        user.setSituationDescription(request.situationDescription());
        user.setStatus("ACTIVE");
        user = users.save(user);
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoles.save(userRole);
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String identifier, String password) {
        String login = identifier.trim();
        User user = users.findByEmailIgnoreCase(login)
                .or(() -> users.findByPhone(login))
                .or(() -> users.findByCitizenId(login))
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
