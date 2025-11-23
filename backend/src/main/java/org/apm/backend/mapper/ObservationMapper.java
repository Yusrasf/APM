package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.ObservationDTO;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.DataType;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.StringType;
import org.springframework.stereotype.Component;

@Component
public class ObservationMapper {

    public ObservationDTO toObservationDTO(Observation obs) {
        if (obs == null) {
            return null;
        }

        ObservationDTO dto = new ObservationDTO();

        // id
        dto.setObservationId(obs.getIdElement().getIdPart());

        // Try to find related Immunization from basedOn or focus
        String immId = extractImmunizationId(obs);
        dto.setImmunizationId(immId);

        // code + display
        if (obs.hasCode()) {
            CodeableConcept cc = obs.getCode();
            if (!cc.getCoding().isEmpty()) {
                Coding coding = cc.getCodingFirstRep();
                dto.setCode(coding.getCode());
                dto.setDisplay(coding.getDisplay());
            } else if (cc.hasText()) {
                dto.setDisplay(cc.getText());
            }
        }

        // value + unit (very simple handling)
        DataType value = obs.getValue();
        if (value instanceof Quantity) {
            Quantity quantity = (Quantity) value;
            if (quantity.getValue() != null) {
                dto.setValue(quantity.getValue().toPlainString());
            }
            dto.setUnit(quantity.getUnit());
        } else if (value instanceof StringType) {
            StringType st = (StringType) value;
            dto.setValue(st.getValue());
        }

        // effectiveDateTime
        if (obs.hasEffectiveDateTimeType()) {
            dto.setEffectiveDateTime(
                    obs.getEffectiveDateTimeType().getValueAsString()
            );
        }

        return dto;
    }

    private String extractImmunizationId(Observation obs) {
        // 1) basedOn references
        for (Reference ref : obs.getBasedOn()) {
            if (ref.getReferenceElement().hasResourceType()
                    && "Immunization".equals(ref.getReferenceElement().getResourceType())
                    && ref.getReferenceElement().hasIdPart()) {
                return ref.getReferenceElement().getIdPart();
            }
        }
        // 2) focus references
        for (Reference ref : obs.getFocus()) {
            if (ref.getReferenceElement().hasResourceType()
                    && "Immunization".equals(ref.getReferenceElement().getResourceType())
                    && ref.getReferenceElement().hasIdPart()) {
                return ref.getReferenceElement().getIdPart();
            }
        }
        return null;
    }
}
