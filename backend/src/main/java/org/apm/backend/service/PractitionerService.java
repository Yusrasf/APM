package org.apm.backend.service;

import org.hl7.fhir.r5.model.Practitioner;
import java.util.Optional;

/**
 * Defines business logic for Practitioner resources.
 */
public interface PractitionerService {

    /**
     * Finds a Practitioner by a unique identifier value (e.g., SSN, License).
     */
    Optional<Practitioner> findPractitionerByIdentifierValue(String identifierValue); // <--- ДОЛЖЕН БЫТЬ ТУТ
}