package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supply_movements")
@Getter @Setter @NoArgsConstructor
public class SupplyMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "donation_item_id", nullable = false)
    private DonationItem donationItem;

    @Column(name = "movement_type", nullable = false, length = 40)
    private String movementType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "from_location_type", length = 30)
    private String fromLocationType;

    @Column(name = "from_location_id")
    private Long fromLocationId;

    @Column(name = "to_location_type", length = 30)
    private String toLocationType;

    @Column(name = "to_location_id")
    private Long toLocationId;

    @Column(name = "reference_type", length = 40)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
