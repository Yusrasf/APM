package org.apm.backend.mapper;

import org.apm.backend.dto.immunization.CreateImmunizationRequestDto;
import org.apm.backend.dto.immunization.ImmunizationDto;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Immunization;
import org.hl7.fhir.r5.model.Reference;
import org.springframework.stereotype.Component;
import org.hl7.fhir.r5.model.DateTimeType;

@Component
public class ImmunizationMapper {

    public Immunization toFHIR(String patientId, CreateImmunizationRequestDto dto) {
        Immunization immunization = new Immunization();

        immunization.setPatient(new Reference("Patient/" + patientId));

        // Vaccine code
        CodeableConcept vaccineCode = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem(dto.getVaccineSystem());
        coding.setCode(dto.getVaccineCode());
        coding.setDisplay(dto.getVaccineDisplay());
        vaccineCode.addCoding(coding);
        vaccineCode.setText(dto.getVaccineDisplay());
        immunization.setVaccineCode(vaccineCode);

        // Occurrence date
        if (dto.getOccurrenceDate() != null && !dto.getOccurrenceDate().isBlank()) {
            immunization.setOccurrence(new DateTimeType(dto.getOccurrenceDate()));
        }

        immunization.setLotNumber(dto.getLotNumber());
        immunization.setStatus(Immunization.ImmunizationStatusCodes.COMPLETED);

        // self-reported → primarySource false
        immunization.setPrimarySource(!dto.isSelfReported());

        // Just store location as text (could later be a real Location reference)
        if (dto.getLocationDisplay() != null) {
            immunization.setLocation(new Reference().setDisplay(dto.getLocationDisplay()));
        }

        return immunization;
    }

    public ImmunizationDto toDto(Immunization immunization) {
        ImmunizationDto dto = new ImmunizationDto();

        if (immunization.getIdElement() != null) {
            dto.setId(immunization.getIdElement().getIdPart());
        }

        if (immunization.hasPatient()) {
            dto.setPatientId(immunization.getPatient().getReferenceElement().getIdPart());
        }

        if (immunization.hasVaccineCode() && immunization.getVaccineCode().hasCoding()) {
            Coding coding = immunization.getVaccineCode().getCodingFirstRep();
            dto.setVaccineSystem(coding.getSystem());
            dto.setVaccineCode(coding.getCode());
            dto.setVaccineDisplay(coding.getDisplay());
        } else if (immunization.hasVaccineCode()) {
            dto.setVaccineDisplay(immunization.getVaccineCode().getText());
        }

        dto.setStatus(immunization.getStatus() != null
                ? immunization.getStatus().toCode()
                : null);

        if (immunization.hasOccurrenceDateTimeType()) {
            dto.setOccurrenceDate(immunization.getOccurrenceDateTimeType().asStringValue());
        }

        dto.setLotNumber(immunization.getLotNumber());

        if (immunization.hasLocation()) {
            dto.setLocationDisplay(immunization.getLocation().getDisplay());
        }

        if (immunization.hasPerformer()) {
            dto.setPerformerDisplay(
                    immunization.getPerformerFirstRep().getActor().getDisplay()
            );
        }

        dto.setSelfReported(!immunization.getPrimarySource());

        return dto;
    }
}
