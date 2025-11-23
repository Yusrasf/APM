package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.ImmunizationDTO;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Immunization;
import org.springframework.stereotype.Component;

@Component
public class ImmunizationMapper {

    public ImmunizationDTO toImmunizationDTO(Immunization imm) {
        if (imm == null) {
            return null;
        }

        ImmunizationDTO dto = new ImmunizationDTO();

        // id
        dto.setImmunizationId(imm.getIdElement().getIdPart());

        // patient
        if (imm.hasPatient() && imm.getPatient().getReferenceElement().hasIdPart()) {
            dto.setPatientId(imm.getPatient().getReferenceElement().getIdPart());
        }

        // encounter
        if (imm.hasEncounter() && imm.getEncounter().getReferenceElement().hasIdPart()) {
            dto.setEncounterId(imm.getEncounter().getReferenceElement().getIdPart());
        }

        // organization: look for performer that is an Organization
        if (imm.hasPerformer()) {
            imm.getPerformer().stream()
                    .filter(p -> p.hasActor()
                            && p.getActor().getReferenceElement().hasResourceType()
                            && "Organization".equals(
                            p.getActor().getReferenceElement().getResourceType()))
                    .findFirst()
                    .ifPresent(p ->
                            dto.setOrganizationId(
                                    p.getActor().getReferenceElement().getIdPart()));
        }

        // vaccine code + display
        if (imm.hasVaccineCode()
                && !imm.getVaccineCode().getCoding().isEmpty()) {
            Coding coding = imm.getVaccineCode().getCodingFirstRep();
            dto.setVaccineCode(coding.getCode());
            dto.setVaccineDisplay(coding.getDisplay());
        } else if (imm.hasVaccineCode() && imm.getVaccineCode().hasText()) {
            dto.setVaccineDisplay(imm.getVaccineCode().getText());
        }

        // occurrence date/time
        if (imm.hasOccurrenceDateTimeType()) {
            dto.setOccurrenceDateTime(
                    imm.getOccurrenceDateTimeType().getValueAsString());
        }

        // status
        if (imm.hasStatus()) {
            dto.setStatus(imm.getStatus().toCode());
        }

        // lot number
        if (imm.hasLotNumber()) {
            dto.setLotNumber(imm.getLotNumber());
        }

        return dto;
    }
}
