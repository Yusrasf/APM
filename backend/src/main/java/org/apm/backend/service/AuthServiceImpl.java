package org.apm.backend.service;

import org.apm.backend.dto.practitioner.LoginRequestDTO;
import org.apm.backend.dto.practitioner.LoginResponseDTO;
import org.apm.backend.dto.practitioner.PractitionerHeaderDTO;
import org.apm.backend.model.PractitionerEntity;
import org.apm.backend.repository.PractitionerRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Needed for hash checking
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService using real data access and password checking.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final PractitionerRepository practitionerRepository;
    private final PasswordEncoder passwordEncoder; // Injecting PasswordEncoder

    public AuthServiceImpl(PractitionerRepository practitionerRepository, PasswordEncoder passwordEncoder) {
        this.practitionerRepository = practitionerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO authenticate(LoginRequestDTO request) {

        // 1. Find user by identifier (login)
        PractitionerEntity entity = practitionerRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Invalid credentials: Practitioner not found"));

        // 2. Password check
        // Compare the provided password (raw) with the hash in the DB
        if (!passwordEncoder.matches(request.getPassword(), entity.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials: Password mismatch");
        }

        // 3. If authentication is successful, create DTO and JWT token

        // In a real application: generate a real JWT using the user ID (entity.getId())
        String realJwtToken = "real_jwt_token_for_user_" + entity.getId() + "_[SECRET_HASH]";

        PractitionerHeaderDTO practitionerDetails = new PractitionerHeaderDTO(
                entity.getId(),
                // Construct the full name
                String.format("%s %s %s", entity.getPrefix(), entity.getGivenName(), entity.getFamilyName()),
                entity.getOrganizationName()
        );

        return new LoginResponseDTO(realJwtToken, practitionerDetails);
    }
}