package org.apm.backend.controller;

import jakarta.annotation.PostConstruct;
import org.apm.backend.dto.practitioner.LoginRequestDTO;
import org.apm.backend.dto.practitioner.LoginResponseDTO;
import org.apm.backend.dto.practitioner.RegistrationRequestDTO;
import org.apm.backend.dto.practitioner.RegistrationResponseDTO;
import org.apm.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all authentication endpoints, such as login and token refreshing.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostConstruct
    public void init() {
        System.out.println("AuthController LOADED");
    }

    // Home page
    @GetMapping("/")
    public String home() {
        return "Hello! API is running.";
    }

    // Login endpoint
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.authenticate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Register endpoint
    @PostMapping("/auth/register")
    public ResponseEntity<RegistrationResponseDTO> register(@RequestBody RegistrationRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok(new RegistrationResponseDTO("Practitioner registered successfully"));
    }
}
