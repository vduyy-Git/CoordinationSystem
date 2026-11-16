package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "donation_items")
@Getter @Setter @NoArgsConstructor
public class DonationItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "offered_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredQuantity;

    @Column(name = "accepted_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "delivered_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveredQuantity = BigDecimal.ZERO;

    @Column(name = "item_condition", length = 50)
    private String itemCondition;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";
}
