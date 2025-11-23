package org.apm.backend.mapper;

import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.DateType;
import org.apm.backend.dto.patient.CreatePatientRequestDto;
import org.apm.backend.dto.patient.PatientSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public Patient toFHIR(CreatePatientRequestDto dto) {
        Patient patient = new Patient();
        if (dto.getIdentifier() != null) {
            patient.addIdentifier().setValue(dto.getIdentifier());
        }
        patient.addName()
                .setFamily(dto.getLastName())
                .addGiven(dto.getFirstName());
        if (dto.getBirthDate() != null) {
            patient.setBirthDateElement(new DateType(dto.getBirthDate()));
        }
        /// mother identifier??
        // Optionally store related person identifier as a second identifier
        if (dto.getRelatedPersonIdentifier() != null
                && !dto.getRelatedPersonIdentifier().isBlank()) {
            patient.addIdentifier()
                    .setSystem("http://example.org/fhir/related-person-identifier")
                    .setValue(dto.getRelatedPersonIdentifier());
        }

        return patient;
    }
    public PatientSummaryDto toPatientSummaryDto(Patient patient) {
        PatientSummaryDto dto = new PatientSummaryDto();

        if (patient.getIdElement() != null) {
            dto.setId(patient.getIdElement().getIdPart());
        }

        if (patient.hasName()) {
            dto.setFullName(patient.getNameFirstRep().getNameAsSingleString());
        }

        if (patient.hasBirthDate()) {
            dto.setBirthDate(patient.getBirthDateElement().asStringValue());
        }

        if (patient.hasIdentifier()) {
            dto.setIdentifier(patient.getIdentifierFirstRep().getValue());
        }

        return dto;
    }

}
