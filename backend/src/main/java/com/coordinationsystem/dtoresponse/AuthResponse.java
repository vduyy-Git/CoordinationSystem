package com.coordinationsystem.dtoresponse;

import java.util.List;

public record AuthResponse(String token, String tokenType, long expiresInSeconds,
                           Long userId, String fullName, String email, List<String> roles) {
}
