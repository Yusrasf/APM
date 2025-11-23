package org.apm.backend.dto.practitioner;

import java.util.List;

public class PatientClinicalOverviewDTO {

    private PatientDetailsDTO patient;                 /// patient details box
    private List<EncounterBlockDTO> encounters;        /// one block per encounter

    // getters & setters...
}
