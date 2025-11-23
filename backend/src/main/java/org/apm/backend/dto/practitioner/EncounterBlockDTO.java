package org.apm.backend.dto.practitioner;

import java.util.List;

public class EncounterBlockDTO {

    private EncounterDTO encounter;                     // encounter box
    private LocationDTO location;                       // location box
    private OrganizationDTO organization;               // organization box
    private List<ImmunizationBlockDTO> immunizations;   // immunizations in this encounter

    // getters & setters...
}

