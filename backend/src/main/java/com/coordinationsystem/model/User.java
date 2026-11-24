package com.coordinationsystem.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(name = "citizen_id", length = 20)
    private String citizenId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String ward;

    @Column(name = "beneficiary_group", length = 100)
    private String beneficiaryGroup;

    @Column(name = "situation_description", columnDefinition = "nvarchar(max)")
    private String situationDescription;

    @Column(length = 500)
    private String avatar;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
