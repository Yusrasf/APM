package org.apm.backend.controller;

import org.apm.backend.dto.practitioner.LoginRequestDTO;
import org.apm.backend.dto.practitioner.LoginResponseDTO;
import org.apm.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/// Handles all authentication endpoints, such as login and token refreshing.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Dependency Injection: Spring automatically injects AuthServiceImpl here.
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for practitioner login.
     * Maps to POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {

        // Delegating business logic to the Service Layer
        LoginResponseDTO response = authService.authenticate(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}