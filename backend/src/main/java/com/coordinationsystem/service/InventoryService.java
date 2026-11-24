package com.coordinationsystem.service;

import com.coordinationsystem.dtoresponse.InventorySummaryResponse;
import com.coordinationsystem.repository.DonationItemRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final DonationItemRepository items;
    public InventoryService(DonationItemRepository items) { this.items = items; }

    @Transactional(readOnly = true)
    public List<InventorySummaryResponse> summary() {
        Map<Long, Totals> totals = new LinkedHashMap<>();
        for (var line : items.findAll()) {
            if (line.getAcceptedQuantity() == null || line.getAcceptedQuantity().signum() == 0) continue;
            var item = line.getItem();
            var category = item.getCategory();
            var unit = item.getUnit();
            Totals total = totals.computeIfAbsent(item.getId(), ignored -> new Totals(item.getId(), item.getName(),
                    category == null ? null : category.getName(), unit == null ? null : unit.getSymbol()));
            total.accepted = total.accepted.add(line.getAcceptedQuantity());
            total.reserved = total.reserved.add(line.getReservedQuantity());
            total.delivered = total.delivered.add(line.getDeliveredQuantity());
        }
        return totals.values().stream().map(Totals::toResponse).toList();
    }

    private static class Totals {
        final Long id; final String name; final String category; final String unit;
        BigDecimal accepted = BigDecimal.ZERO, reserved = BigDecimal.ZERO, delivered = BigDecimal.ZERO;
        Totals(Long id, String name, String category, String unit) { this.id = id; this.name = name; this.category = category; this.unit = unit; }
        InventorySummaryResponse toResponse() { return new InventorySummaryResponse(id, name, category, unit, accepted, reserved, delivered, accepted.subtract(reserved).subtract(delivered)); }
    }
}
