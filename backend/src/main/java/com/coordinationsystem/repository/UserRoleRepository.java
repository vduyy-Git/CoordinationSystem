package com.coordinationsystem.repository;

import com.coordinationsystem.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    @EntityGraph(attributePaths = {"role", "user"})
    List<UserRole> findByUser_Id(Long userId);

    @EntityGraph(attributePaths = {"role", "user"})
    Optional<UserRole> findByUser_EmailIgnoreCaseAndRole_RoleName(String email, String roleName);
}
