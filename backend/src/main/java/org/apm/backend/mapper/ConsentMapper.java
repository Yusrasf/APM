package org.apm.backend.mapper;

import org.apm.backend.dto.consent.ConsentDto;
import org.apm.backend.dto.consent.UpdateConsentRequestDto;
import org.hl7.fhir.r5.model.Consent;
import org.hl7.fhir.r5.model.Reference;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ConsentMapper {

    public Consent toFHIR(String patientId, UpdateConsentRequestDto dto) {
        Consent consent = new Consent();

        // Link to patient
        consent.setSubject(new Reference("Patient/" + patientId));

        // Simple status handling
        consent.setStatus(Consent.ConsentState.ACTIVE);

        // R5 uses "date" (not dateTime) for the consent date
        consent.setDate(new Date());

        // Use category.text entries to encode our “scope” + booleans
        consent.addCategory()
                .setText("vaccination-registry-consent");

        consent.addCategory()
                .setText(dto.isShareWithPhysicians()
                        ? "share-with-physicians"
                        : "no-share-with-physicians");

        consent.addCategory()
                .setText(dto.isShareWithPublicHealth()
                        ? "share-with-public-health"
                        : "no-share-with-public-health");

        return consent;
    }

    public ConsentDto toDto(Consent consent) {
        ConsentDto dto = new ConsentDto();

        if (consent.getIdElement() != null) {
            dto.setId(consent.getIdElement().getIdPart());
        }

        if (consent.hasSubject()) {
            dto.setPatientId(
                    consent.getSubject().getReferenceElement().getIdPart()
            );
        }

        dto.setStatus(consent.getStatus() != null ? consent.getStatus().toCode() : null);

        // R5: "date", not "dateTime"
        if (consent.hasDate()) {
            dto.setLastUpdated(consent.getDateElement().asStringValue());
        }

        // Decode our booleans from the category texts
        boolean shareWithPhysicians = consent.getCategory().stream()
                .anyMatch(cc -> "share-with-physicians".equals(cc.getText()));
        boolean shareWithPublicHealth = consent.getCategory().stream()
                .anyMatch(cc -> "share-with-public-health".equals(cc.getText()));

        dto.setShareWithPhysicians(shareWithPhysicians);
        dto.setShareWithPublicHealth(shareWithPublicHealth);

        return dto;
    }
}
