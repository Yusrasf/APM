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
        patient.addIdentifier().setValue(dto.getIdentifier());
        patient.addName()
                .setFamily(dto.getLastName())
                .addGiven(dto.getFirstName());
        patient.setBirthDateElement(new DateType(dto.getBirthDate()));
        /// mother identifier???
        patient.addLink().setValue(dto.getrelate);
        return patient;
    }
    public PatientSummaryDto toPatientSummaryDto(Patient patient) {
        PatientSummaryDto dto = new PatientSummaryDto();
        dto.setId(patient.getIdElement().getIdPart());
        dto.setFullName(patient.getNameFirstRep().getNameAsSingleString());
        dto.setBirthDate(patient.getBirthDateElement().asStringValue());
        dto.setIdentifier(patient.getIdentifierFirstRep().getValue());
        return dto;
    }

}
