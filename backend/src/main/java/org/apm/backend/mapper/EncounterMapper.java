package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.EncounterDTO;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.CodeableReference;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Period;
import org.springframework.stereotype.Component;

@Component
public class EncounterMapper {

    public EncounterDTO toEncounterDTO(Encounter encounter) {
        if (encounter == null) {
            return null;
        }

        EncounterDTO dto = new EncounterDTO();

        // --- id ---
        dto.setEncounterId(encounter.getIdElement().getIdPart());

        // --- subject (patient id) ---
        if (encounter.hasSubject()
                && encounter.getSubject().getReferenceElement().hasIdPart()) {
            dto.setPatientId(encounter.getSubject().getReferenceElement().getIdPart());
        }

        // --- status ---
        if (encounter.hasStatus()) {
            dto.setStatus(encounter.getStatus().toCode());
        }

        // --- reason (R5: Encounter.reason.value : List<CodeableReference>) ---
        if (encounter.hasReason()) {
            Encounter.ReasonComponent reasonComponent = encounter.getReasonFirstRep();

            if (reasonComponent.hasValue() && !reasonComponent.getValue().isEmpty()) {
                // take the first CodeableReference
                CodeableReference valueRef = reasonComponent.getValueFirstRep();

                if (valueRef.getConcept() != null) {
                    CodeableConcept concept = valueRef.getConcept();

                    if (!concept.getCoding().isEmpty()) {
                        Coding coding = concept.getCodingFirstRep();
                        dto.setReasonCode(coding.getCode());
                        dto.setReasonDisplay(coding.getDisplay());
                    } else if (concept.hasText()) {
                        dto.setReasonDisplay(concept.getText());
                    }
                }
            }
        }

        // --- period (R5: actualPeriod) ---
        if (encounter.hasActualPeriod()) {
            Period p = encounter.getActualPeriod();

            if (p.hasStart()) {
                dto.setStartDateTime(p.getStartElement().getValueAsString());
            }
        }

        return dto;
    }
}
