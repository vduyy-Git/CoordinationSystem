package com.coordinationsystem.dtorequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.util.List;

public record CreateAllocationRequest(
        @NotNull Long requestId,
        @Size(max = 2000) String note,
        @NotEmpty List<@Valid AllocationLine> items) {
    public record AllocationLine(@NotNull Long requestItemId, @NotNull Long donationItemId,
                                 @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal quantity) { }
}
