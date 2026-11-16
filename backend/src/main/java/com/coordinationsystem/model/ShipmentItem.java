package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "shipment_items")
@Getter @Setter @NoArgsConstructor
public class ShipmentItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "allocation_item_id", nullable = false)
    private AllocationItem allocationItem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;
}
