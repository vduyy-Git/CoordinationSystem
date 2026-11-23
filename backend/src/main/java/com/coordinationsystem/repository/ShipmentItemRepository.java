package com.coordinationsystem.repository;

import com.coordinationsystem.model.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {
    List<ShipmentItem> findByShipment_Id(Long shipmentId);
    List<ShipmentItem> findByAllocationItem_Id(Long allocationItemId);

    @Query("select coalesce(sum(si.quantity), 0) from ShipmentItem si where si.allocationItem.id = :id and si.shipment.status <> 'CANCELLED'")
    BigDecimal sumShippedForAllocationItem(@Param("id") Long allocationItemId);

    @Query("select coalesce(sum(si.quantity), 0) from ShipmentItem si where si.allocationItem.requestItem.id = :id and si.shipment.status = 'DELIVERED'")
    BigDecimal sumDeliveredForRequestItem(@Param("id") Long requestItemId);

    @Query("select coalesce(sum(si.quantity), 0) from ShipmentItem si where si.allocationItem.id = :id and si.shipment.status = 'DELIVERED'")
    BigDecimal sumDeliveredForAllocationItem(@Param("id") Long allocationItemId);
}
