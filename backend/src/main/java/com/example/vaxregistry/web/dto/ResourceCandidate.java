package com.example.vaxregistry.web.dto;

/**
 * A minimal representation of a FHIR resource for "pick one" flows.
 */
public record ResourceCandidate(
        String id,
        String display,
        String birthDate,
        String identifier
) {}
