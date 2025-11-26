package org.apm.backend.dto.practitioner;

import java.util.List;
/**
 * DTO representing one Encounter block in the Patient Clinical Overview.
 *
 * Contains:
 *  - EncounterDTO          : main encounter information
 *  - LocationDTO           : where the encounter happened
 *  - OrganizationDTO       : hospital/clinic
 *  - List<ImmunizationBlockDTO> : immunizations that happened in this encounter
 */
public class EncounterBlockDTO {

    private EncounterDTO encounter;
    private LocationDTO location;                       /// from Immunization.location
    private OrganizationDTO organization;               /// from Immunization.performer
    private List<ImmunizationBlockDTO> immunizations;   /// all immunizations in this encounter

    public EncounterDTO getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterDTO encounter) {
        this.encounter = encounter;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    public OrganizationDTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDTO organization) {
        this.organization = organization;
    }

    public List<ImmunizationBlockDTO> getImmunizations() {
        return immunizations;
    }

    public void setImmunizations(List<ImmunizationBlockDTO> immunizations) {
        this.immunizations = immunizations;
    }
}
