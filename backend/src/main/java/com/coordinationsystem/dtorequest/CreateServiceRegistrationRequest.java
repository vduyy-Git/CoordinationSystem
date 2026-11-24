package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateServiceRegistrationRequest(@NotNull Long serviceId,
                                               @Size(max = 10000) String requestDetails) { }
