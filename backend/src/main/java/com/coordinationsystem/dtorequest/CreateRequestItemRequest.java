package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record CreateRequestItemRequest(@NotNull Long requestId, @NotNull Long itemId,
                                       @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal requestedQuantity,
                                       @Size(max = 10000) String note) { }
