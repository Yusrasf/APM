package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.PatientDetailsDTO;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    /// PATIENT USER SIDE



    /// PRACTITIONER USER SIDE
    /// 1) Patient -> PatientDetailsDTO (clinical overview header for Practitioner)
    public PatientDetailsDTO toPatientDetailsDTO(Patient patient) {
        if (patient == null) {
            return null;
        }

        PatientDetailsDTO dto = new PatientDetailsDTO();

        /// Patient Technical FHIR id (Patient.id)
        if (patient.getIdElement() != null) {
            dto.setPatientId(patient.getIdElement().getIdPart());
        }
        /// Patient Business identifier (Austrian social security number)
        if (patient.hasIdentifier()) {
            Identifier identifier = patient.getIdentifierFirstRep();
            dto.setPatientIdentifier(identifier.getValue());
        }

        /// Patient Name (firstName + lastName)
        if (!patient.getName().isEmpty()) {
            HumanName name = patient.getNameFirstRep();
            dto.setFirstName(name.getGivenAsSingleString());
            dto.setLastName(name.getFamily());
        }
        /// Patient Birth date
        if (patient.hasBirthDate()) {
            dto.setBirthDate(patient.getBirthDateElement().getValueAsString());
        }
        /// Patient Gender
        if (patient.getGender() != null) {
            dto.setGender(patient.getGender().toCode()); // "male", "female", "other", "unknown"
        }
        return dto;
    }


}