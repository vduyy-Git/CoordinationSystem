package com.coordinationsystem.service;

import com.coordinationsystem.dtoresponse.ProfileResponse;
import com.coordinationsystem.dtorequest.ChangePasswordRequest;
import com.coordinationsystem.dtorequest.UpdateProfileRequest;
import com.coordinationsystem.exception.BadRequestException;
import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.exception.UnAuthorizeException;
import com.coordinationsystem.model.User;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.UserRoleRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileService {
    private final UserRepository users;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder passwords;

    public ProfileService(UserRepository users, UserRoleRepository userRoles, PasswordEncoder passwords) {
        this.users = users;
        this.userRoles = userRoles;
        this.passwords = passwords;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(String email) { return view(find(email)); }

    public ProfileResponse update(String currentEmail, UpdateProfileRequest request) {
        User user = find(currentEmail);
        String newEmail = request.email() == null || request.email().isBlank()
                ? user.getEmail() : request.email().trim().toLowerCase(Locale.ROOT);
        if (!user.getEmail().equalsIgnoreCase(newEmail))
            throw new BadRequestException("Không thể đổi email trong luồng cập nhật hồ sơ");
        user.setFullName(request.fullName().trim());
        user.setEmail(newEmail);
        user.setPhone(request.phone());
        String citizenId = request.citizenId() == null || request.citizenId().isBlank()
                ? null : request.citizenId().trim();
        if (citizenId != null && !Objects.equals(citizenId, user.getCitizenId())
                && users.existsByCitizenId(citizenId)) {
            throw new com.coordinationsystem.exception.ConflictException("CCCD đã được sử dụng");
        }
        user.setCitizenId(citizenId);
        user.setBirthDate(request.birthDate());
        user.setAddress(request.address());
        user.setWard(request.ward());
        user.setBeneficiaryGroup(request.beneficiaryGroup());
        user.setSituationDescription(request.situationDescription());
        user.setAvatar(request.avatar());
        return view(users.save(user));
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = find(email);
        if (!passwords.matches(request.currentPassword(), user.getPassword()))
            throw new UnAuthorizeException("Mật khẩu hiện tại không đúng");
        if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new BadRequestException("Mật khẩu mới không được vượt quá 72 byte UTF-8");
        user.setPassword(passwords.encode(request.newPassword()));
        users.save(user);
    }

    private User find(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
    }

    private ProfileResponse view(User user) {
        var roles = userRoles.findByUser_Id(user.getId()).stream().map(r -> r.getRole().getRoleName()).toList();
        return new ProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                user.getCitizenId(), user.getBirthDate(), user.getAddress(), user.getWard(),
                user.getBeneficiaryGroup(), user.getSituationDescription(), user.getAvatar(),
                user.getStatus(), roles, user.getCreatedAt());
    }
}
