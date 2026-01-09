package com.sgt.fitapi.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
@SecurityRequirements({})
public class HelloController {
    @GetMapping("/hello")
    @Operation(
            summary = "Hello endpoint",
            description = "Returns a friendly greeting to verify the API is reachable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public String hello() {
        return "Hello, FitAPI!";
    }
}
