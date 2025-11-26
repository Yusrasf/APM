package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.OrganizationDTO;
import org.hl7.fhir.r5.model.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public OrganizationDTO toOrganizationDTO(Organization org) {
        if (org == null) {
            return null;
        }

        OrganizationDTO dto = new OrganizationDTO();
        // id
        if (org.getIdElement() != null) {
            dto.setOrganizationId(org.getIdElement().getIdPart());
        }
        // name
        dto.setName(org.getName());
        return dto;
    }
}
