package org.apm.backend.service;
import org.hl7.fhir.r5.model.StringType;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Practitioner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FhirUserDetailsService implements UserDetailsService {

    private final IGenericClient fhirClient;

    public FhirUserDetailsService(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Search practitioner by identifier in FHIR
        Bundle bundle = fhirClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().identifier(identifier))
                .returnBundle(Bundle.class)
                .execute();

        if (!bundle.hasEntry()) {
            throw new UsernameNotFoundException("Practitioner not found: " + identifier);
        }

        Practitioner practitioner = (Practitioner) bundle.getEntryFirstRep().getResource();

        // Extract password from extension
        String password = practitioner.getExtension().stream()
                .filter(ext -> "http://example.org/extensions/password".equals(ext.getUrl()))
                .map(ext -> ((org.hl7.fhir.r5.model.StringType) ext.getValue()).getValue())
                .findFirst()
                .orElse(""); // Default empty if no extension

        // Return UserDetails for Spring Security
        return User.builder()
                .username(identifier)
                .password(password)
                .roles("PRACTITIONER")
                .build();
    }
}