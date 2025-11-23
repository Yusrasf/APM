package org.apm.backend.dto.practitioner;

import java.util.List;

/**
 * Root DTO for the Patient Clinical Overview screen.
 * Structure:
 *  - patient  : basic patient details (header)
 *  - encounters : list of encounter blocks, each containing
 *      EncounterDTO + LocationDTO + OrganizationDTO + ImmunizationBlockDTOs
 */
public class PatientClinicalOverviewDTO {

    private PatientDetailsDTO patient;
    private List<EncounterBlockDTO> encounters;

    // --- getters & setters ---

    public PatientDetailsDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDetailsDTO patient) {
        this.patient = patient;
    }

    public List<EncounterBlockDTO> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<EncounterBlockDTO> encounters) {
        this.encounters = encounters;
    }
}
