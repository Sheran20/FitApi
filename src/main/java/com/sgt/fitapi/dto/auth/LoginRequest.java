package com.sgt.fitapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public class LoginRequest {

    @Email
    @NotBlank
    @Schema(description = "User email address used for login.", example = "user@example.com")
    public String email;

    @NotBlank
    @Schema(description = "User password.", example = "Str0ngPassw0rd!")
    public String password;
}
