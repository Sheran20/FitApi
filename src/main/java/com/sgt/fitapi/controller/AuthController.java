package com.sgt.fitapi.controller;

import com.sgt.fitapi.dto.auth.AuthResponse;
import com.sgt.fitapi.dto.auth.LoginRequest;
import com.sgt.fitapi.dto.auth.RegisterRequest;
import com.sgt.fitapi.model.User;
import com.sgt.fitapi.repository.UserRepository;
import com.sgt.fitapi.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // basic uniqueness check
        if (userRepository.existsByEmail(request.email)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse("Email already in use"));
        }

        // hash the password before saving
        String hashedPassword = passwordEncoder.encode(request.password);

        User user = new User(
                request.email,
                hashedPassword,
                request.displayName
        );

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse("User registered successfully", token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // find user by email
        User user = userRepository.findByEmail(request.email)
                .orElse(null);

        if (user == null) {
            // don't reveal whether email exists; generic error
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid email or password"));
        }

        // check password using PasswordEncoder
        boolean passwordMatches = passwordEncoder.matches(
                request.password,
                user.getPassword()
        );

        if (!passwordMatches) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid email or password"));
        }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse("Login successful", token));
    }

    // optional sanity check endpoint
    @GetMapping("/ping")
    public AuthResponse ping() {
        return new AuthResponse("Auth API is alive");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        // No authentication or principal is not our User -> treat as "not logged in"
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.ok(
                    java.util.Map.of(
                            "authenticated", false
                    )
            );
        }

        // Logged-in user -> return their info
        return ResponseEntity.ok(
                java.util.Map.of(
                        "authenticated", true,
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "displayName", user.getDisplayName(),
                        "role", user.getRole()
                )
        );
    }


}
