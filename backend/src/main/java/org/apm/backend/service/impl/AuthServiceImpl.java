package org.apm.backend.service.impl;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apm.backend.dto.practitioner.*;
import org.hl7.fhir.r5.model.*;
import org.apm.backend.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final IGenericClient fhirClient;

    public AuthServiceImpl(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    @Override
    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        System.out.println("Authenticating: " + request.getIdentifier());

        // 1. Find practitioner
        Bundle bundle = fhirClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().systemAndIdentifier(
                        "http://hospital.smarthealthit.org/practitioners",
                        request.getIdentifier()
                ))
                .returnBundle(Bundle.class)
                .execute();
        System.out.println("DEBUG: Searching for identifier: " + request.getIdentifier());
        System.out.println("DEBUG: Bundle has entries? " + bundle.hasEntry());
        System.out.println("DEBUG: Bundle total: " + bundle.getTotal());
        System.out.println("DEBUG: Bundle entries count: " + bundle.getEntry().size());
        if (!bundle.hasEntry()) {
            throw new RuntimeException("Practitioner not found");
        }

        Practitioner practitioner = (Practitioner) bundle.getEntryFirstRep().getResource();

        // 2. Check password from extension
        String storedPassword = null;
        for (Extension ext : practitioner.getExtension()) {
            if ("http://example.org/extensions/password".equals(ext.getUrl())) {
                storedPassword = ((StringType) ext.getValue()).getValue();
                break;
            }
        }

        if (storedPassword == null || !storedPassword.equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 3. Create response
        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken("token-" + practitioner.getIdElement().getIdPart());

        // Create PractitionerHeaderDTO
        PractitionerHeaderDTO practitionerHeader = new PractitionerHeaderDTO();
        practitionerHeader.setId(Long.valueOf(practitioner.getIdElement().getIdPart()));

        if (practitioner.hasName()) {
            HumanName name = practitioner.getNameFirstRep();
            String fullName = name.getGivenAsSingleString() + " " + name.getFamily();
            practitionerHeader.setFullName(fullName.trim());
        }

        response.setPractitioner(practitionerHeader);
        return response;
    }

    @Override
    public void register(RegistrationRequestDTO request) {
        if (request.getIdentifier() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Identifier and password are required");
        }

        // 1. Check if practitioner already exists
        Bundle bundle = fhirClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().systemAndIdentifier(
                        "http://hospital.smarthealthit.org/practitioners",
                        request.getIdentifier()
                ))
                .returnBundle(Bundle.class)
                .execute();

        if (bundle.hasEntry()) {
            throw new IllegalArgumentException("Practitioner with this identifier already exists");
        }

        // 2. Create Practitioner resource
        Practitioner practitioner = new Practitioner();
        practitioner.addIdentifier()
                .setSystem("http://hospital.smarthealthit.org/practitioners")
                .setValue(request.getIdentifier());

        // Name
        if (request.getFirstName() != null || request.getLastName() != null) {
            HumanName name = practitioner.addName();
            if (request.getFirstName() != null) {
                name.addGiven(request.getFirstName());
            }
            if (request.getLastName() != null) {
                name.setFamily(request.getLastName());
            }
        }

        // Optional: store password in a custom extension (for demo)
        if (request.getPassword() != null) {
            practitioner.addExtension()
                    .setUrl("http://example.org/extensions/password")
                    .setValue(new StringType(request.getPassword()));
        }

        // 3. Save to FHIR server
        MethodOutcome outcome = fhirClient.create()
                .resource(practitioner)
                .execute();

        System.out.println("Registered practitioner with ID: " + outcome.getId().getIdPart());
    }
}