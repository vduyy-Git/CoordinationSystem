package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_registrations")
@Getter @Setter @NoArgsConstructor
public class StaffRegistration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", nullable = false, length = 150) private String fullName;
    @Column(nullable = false, unique = true, length = 150) private String email;
    @Column(length = 20) private String phone;
    @Column(nullable = false, length = 150) private String department;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(nullable = false, length = 30) private String status = "PENDING";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private User reviewedBy;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;
}
