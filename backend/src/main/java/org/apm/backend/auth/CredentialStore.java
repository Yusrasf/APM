package org.apm.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CredentialStore {

    private final Map<String, PractitionerCredential> byIdentifier = new HashMap<>();

    public CredentialStore(ObjectMapper objectMapper) {
        try (InputStream is = getClass().getResourceAsStream("/practitioner-credentials.json")) {

            if (is == null) {
                System.out.println("DEBUG: practitioner-credentials.json NOT FOUND");
                return;
            }

            PractitionerCredential[] all =
                    objectMapper.readValue(is, PractitionerCredential[].class);

            for (PractitionerCredential c : all) {
                byIdentifier.put(c.getIdentifier(), c);
            }

            System.out.println("DEBUG: Loaded " + all.length + " practitioner credentials");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load practitioner-credentials.json", e);
        }
    }

    public Optional<PractitionerCredential> findByIdentifier(String identifier) {
        return Optional.ofNullable(byIdentifier.get(identifier));
    }
}
