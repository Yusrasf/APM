package org.apm.backend.dto.practitioner;

import java.util.List;

public class ImmunizationBlockDTO {

    private ImmunizationDTO immunization;          // immunization box
    private PractitionerDTO practitioner;          // practitioner for this immunization
    private MedicationDTO medication;              // medication/vaccine product
    private List<ObservationDTO> observations;     // observations linked to this immunization

    // getters & setters...
}
