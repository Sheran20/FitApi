package com.sgt.fitapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterRequest {

    @Email
    @NotBlank
    @Schema(description = "User email address. Must be unique.", example = "new.user@example.com")
    public String email;

    @NotBlank
    @Size(min = 8, max = 64)
    @Schema(description = "Account password (8-64 characters).", example = "Str0ngPassw0rd!")
    public String password;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "Display name shown in the app.", example = "New User")
    public String displayName;
}
