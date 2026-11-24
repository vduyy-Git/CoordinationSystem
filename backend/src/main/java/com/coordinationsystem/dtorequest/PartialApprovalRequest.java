package com.coordinationsystem.dtorequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record PartialApprovalRequest(@NotEmpty List<@Valid Line> items, @Size(max = 10000) String reason) {
    public record Line(@NotNull Long requestItemId, @NotNull @PositiveOrZero BigDecimal approvedQuantity) { }
}
