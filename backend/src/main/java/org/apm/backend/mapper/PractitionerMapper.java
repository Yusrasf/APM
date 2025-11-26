package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.PractitionerDTO;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Practitioner;
import org.springframework.stereotype.Component;

@Component
public class PractitionerMapper {

    public PractitionerDTO toPractitionerDTO(Practitioner practitioner) {
        if (practitioner == null) {
            return null;
        }

        PractitionerDTO dto = new PractitionerDTO();

        // id
        dto.setPractitionerId(practitioner.getIdElement().getIdPart());

        // business identifier (first identifier)
        if (practitioner.hasIdentifier()) {
            Identifier id = practitioner.getIdentifierFirstRep();
            dto.setPractitionerIdentifier(id.getValue());
        }

        // name
        if (!practitioner.getName().isEmpty()) {
            HumanName name = practitioner.getNameFirstRep();
            dto.setFirstName(name.getGivenAsSingleString());
            dto.setLastName(name.getFamily());
        }

        return dto;
    }
}
