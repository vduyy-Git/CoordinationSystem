package com.coordinationsystem.dtoresponse;

import java.math.BigDecimal;

public record InventorySummaryResponse(Long itemId, String itemName, String category, String unit,
                                       BigDecimal accepted, BigDecimal reserved, BigDecimal delivered,
                                       BigDecimal available) { }
