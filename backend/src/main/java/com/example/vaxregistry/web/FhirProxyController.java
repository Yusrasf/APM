package com.example.vaxregistry.web;

import com.example.vaxregistry.service.FhirClientService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class FhirProxyController {

    private static final MediaType FHIR_JSON = MediaType.parseMediaType("application/fhir+json");
    private static final MediaType JSON_PATCH = MediaType.parseMediaType("application/json-patch+json");

    // Whitelist to avoid proxying arbitrary paths
    private static final Set<String> ALLOWED = Set.of(
            "Patient",
            "Practitioner",
            "Organization",
            "Immunization",
            "ImmunizationRecommendation",
            "Consent",
            "AdverseEvent",
            "Encounter",
            "Appointment",
            "Schedule",
            "Slot",
            "ServiceRequest",
            "PractitionerRole",
            "Location",
            "AllergyIntolerance",
            "Condition"
    );

    private final FhirClientService fhir;

    public FhirProxyController(FhirClientService fhir) {
        this.fhir = fhir;
    }

    private static void requireAllowed(String resourceType) {
        if (!ALLOWED.contains(resourceType)) {
            throw new IllegalArgumentException("Unsupported resourceType: " + resourceType);
        }
    }

    @GetMapping(value = "/{resourceType}", produces = "application/fhir+json")
    public ResponseEntity<String> search(
            @PathVariable String resourceType,
            @RequestParam MultiValueMap<String, String> params
    ) {
        requireAllowed(resourceType);
        return fhir.search(resourceType, params);
    }

    @GetMapping(value = "/{resourceType}/{id}", produces = "application/fhir+json")
    public ResponseEntity<String> read(@PathVariable String resourceType, @PathVariable String id) {
        requireAllowed(resourceType);
        return fhir.read(resourceType, id);
    }

    @PostMapping(value = "/{resourceType}", consumes = "application/fhir+json", produces = "application/fhir+json")
    public ResponseEntity<String> create(@PathVariable String resourceType, @RequestBody String body) {
        requireAllowed(resourceType);
        return fhir.create(resourceType, body);
    }

    @PutMapping(value = "/{resourceType}/{id}", consumes = "application/fhir+json", produces = "application/fhir+json")
    public ResponseEntity<String> update(@PathVariable String resourceType, @PathVariable String id, @RequestBody String body) {
        requireAllowed(resourceType);
        return fhir.update(resourceType, id, body);
    }

    @PatchMapping(value = "/{resourceType}/{id}", consumes = "application/json-patch+json", produces = "application/fhir+json")
    public ResponseEntity<String> patch(@PathVariable String resourceType, @PathVariable String id, @RequestBody String body) {
        requireAllowed(resourceType);
        return fhir.patch(resourceType, id, body);
    }

    @DeleteMapping(value = "/{resourceType}/{id}", produces = "application/fhir+json")
    public ResponseEntity<String> delete(@PathVariable String resourceType, @PathVariable String id) {
        requireAllowed(resourceType);
        return fhir.delete(resourceType, id);
    }
}
