package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "social_services")
@Getter @Setter @NoArgsConstructor
public class SocialService {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType = "SOCIAL_SERVICE";

    @Column(name = "service_area", length = 150)
    private String serviceArea;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";
}
