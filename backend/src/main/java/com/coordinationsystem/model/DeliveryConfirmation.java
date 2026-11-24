package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_confirmations")
@Getter @Setter @NoArgsConstructor
public class DeliveryConfirmation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "received_by", nullable = false, length = 150)
    private String receivedBy;

    @Column(name = "confirmation_code", length = 100)
    private String confirmationCode;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(columnDefinition = "nvarchar(max)")
    private String note;

    @Column(name = "delivery_condition", length = 30)
    private String deliveryCondition;

    @Column
    private Integer rating;

    @Column(name = "confirmed_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime confirmedAt;
}
