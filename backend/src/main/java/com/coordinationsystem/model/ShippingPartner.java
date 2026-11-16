package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shipping_partners")
@Getter @Setter @NoArgsConstructor
public class ShippingPartner {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_name", nullable = false, length = 150)
    private String partnerName;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String website;

    @Column(name = "api_endpoint", length = 1000)
    private String apiEndpoint;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";
}
