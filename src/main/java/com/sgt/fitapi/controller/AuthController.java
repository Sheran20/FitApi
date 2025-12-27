package com.sgt.fitapi.controller;

import com.sgt.fitapi.dto.auth.AuthResponse;
import com.sgt.fitapi.dto.auth.LoginRequest;
import com.sgt.fitapi.dto.auth.RegisterRequest;
import com.sgt.fitapi.model.User;
import com.sgt.fitapi.repository.UserRepository;
import com.sgt.fitapi.security.LoginRateLimiter;
import com.sgt.fitapi.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          LoginRateLimiter loginRateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    @SecurityRequirements({})
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns a JWT for immediate use.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "RegisterRequest",
                                    value = "{\n  \"email\": \"new.user@example.com\",\n  \"password\": \"Str0ngPassw0rd!\",\n  \"displayName\": \"New User\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "RegisterSuccess",
                                    value = "{\n  \"message\": \"User registered successfully\",\n  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already in use",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "RegisterConflict",
                                    value = "{\n  \"message\": \"Email already in use\"\n}"
                            )
                    )
            )
    })
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
    @SecurityRequirements({})
    @Operation(
            summary = "Login and get a JWT",
            description = "Authenticates the user and returns a JWT when credentials are valid.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "LoginRequest",
                                    value = "{\n  \"email\": \"user@example.com\",\n  \"password\": \"Str0ngPassw0rd!\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "LoginSuccess",
                                    value = "{\n  \"message\": \"Login successful\",\n  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "LoginInvalid",
                                    value = "{\n  \"message\": \"Invalid email or password\"\n}"
                            )
                    )
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String key = request.email.toLowerCase();
        loginRateLimiter.throwIfBlocked(key);

        // find user by email
        User user = userRepository.findByEmail(request.email)
                .orElse(null);

        if (user == null) {
            loginRateLimiter.onFailure(key);
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
            loginRateLimiter.onFailure(key);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid email or password"));
        }

        loginRateLimiter.onSuccess(key);
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse("Login successful", token));
    }

    // optional sanity check endpoint
    @GetMapping("/ping")
    @SecurityRequirements({})
    @Operation(
            summary = "Auth service health check",
            description = "Simple unauthenticated ping to verify the auth API is responding."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public AuthResponse ping() {
        return new AuthResponse("Auth API is alive");
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's profile data derived from the JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
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
