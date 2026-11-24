package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import java.util.List;

public record CreateDonationRequest(@NotBlank @Size(max = 500) String pickupAddress,
                                    @NotBlank @Size(max = 30) String donationType,
                                    @Size(max = 10000) String note,
                                    @Valid List<Line> items) {
    public record Line(@NotNull Long itemId, @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal offeredQuantity,
                       @Size(max = 50) String itemCondition) { }
}
