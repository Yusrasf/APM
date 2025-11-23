package org.apm.backend.service;

import org.apm.backend.dto.practitioner.LoginRequestDTO;
import org.apm.backend.dto.practitioner.LoginResponseDTO;
import org.apm.backend.dto.practitioner.PractitionerHeaderDTO;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthService using mock data for initial development.
 * This class will contain the real business logic in the future.
 */
@Service
public class AuthServiceImpl implements AuthService {

    // --- Mock Authentication Logic ---

    @Override
    public LoginResponseDTO authenticate(LoginRequestDTO request) {

        System.out.println("Attempting mock authentication for identifier: " + request.getIdentifier());

        // In a real application:
        // 1. Check if the identifier exists in the database.
        // 2. Verify the password hash.
        // 3. If successful, generate a real JWT token.
        // 4. Fetch the Practitioner's details.

        // MOCK LOGIC: Always succeeds with mock data if a password is provided.
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        // 1. Create Mock Practitioner Header DTO
        PractitionerHeaderDTO practitionerDetails = new PractitionerHeaderDTO(
                202L,
                "Dr. Alice Smith",
                "Regional Health Services"
        );

        // 2. Create Mock JWT Token
        String mockToken = "mock_jwt_token_for_user_" + practitionerDetails.getId() + "_abcdefg";

        // 3. Build the Response DTO
        return new LoginResponseDTO(mockToken, practitionerDetails);
    }
}