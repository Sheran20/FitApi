package com.sgt.fitapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @Email
    @NotBlank
    public String email;

    @NotBlank
    @Size(min = 8, max = 64)
    public String password;

    @NotBlank
    @Size(max = 64)
    public String displayName;
}
