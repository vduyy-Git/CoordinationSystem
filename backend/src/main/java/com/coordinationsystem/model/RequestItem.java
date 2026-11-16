package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "request_items")
@Getter @Setter @NoArgsConstructor
public class RequestItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "request_id", nullable = false)
    private AssistanceRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "requested_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedQuantity;

    @Column(name = "approved_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal approvedQuantity = BigDecimal.ZERO;

    @Column(columnDefinition = "nvarchar(max)")
    private String note;
}
