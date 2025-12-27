package com.sgt.fitapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthResponse {
    @Schema(description = "Human-readable status message.", example = "Login successful")
    public String message;
    @Schema(description = "JWT token to authenticate subsequent requests.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    public String token;

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }
}
