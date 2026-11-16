package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "allocation_items")
@Getter @Setter @NoArgsConstructor
public class AllocationItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "allocation_id", nullable = false)
    private Allocation allocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "request_item_id", nullable = false)
    private RequestItem requestItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "donation_item_id", nullable = false)
    private DonationItem donationItem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;
}
