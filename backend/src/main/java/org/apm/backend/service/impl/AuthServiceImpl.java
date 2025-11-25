package org.apm.backend.service;

import org.apm.backend.auth.CredentialStore;
import org.apm.backend.auth.PractitionerCredential;
import org.apm.backend.dto.practitioner.LoginRequestDTO;
import org.apm.backend.dto.practitioner.LoginResponseDTO;
import org.apm.backend.dto.practitioner.PractitionerHeaderDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final CredentialStore credentialStore;

    public AuthServiceImpl(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO authenticate(LoginRequestDTO request) {

        System.out.println("DEBUG: Login attempt identifier=" + request.getIdentifier());

        PractitionerCredential cred = credentialStore
                .findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Invalid credentials (identifier not found)"));

        // PLAIN PASSWORD COMPARISON FOR DEV
        if (!cred.getPassword().equals(request.getPassword())) {
            System.out.println("DEBUG: Password mismatch. Expected=" + cred.getPassword()
                    + ", got=" + request.getPassword());
            throw new RuntimeException("Invalid credentials (password mismatch)");
        }

        // For now, hardcode some practitioner header info
        PractitionerHeaderDTO practitionerDetails = new PractitionerHeaderDTO(
                1L,
                "Practitioner " + request.getIdentifier(),
                "Demo Organization"
        );

        String token = "dummy_token_for_" + request.getIdentifier();

        return new LoginResponseDTO(token, practitionerDetails);
    }
}
