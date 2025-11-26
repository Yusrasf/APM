package org.apm.backend.dto.practitioner;

import java.util.List;

/**
 * DTO representing one Immunization "block" in the Patient Clinical Overview.
 *
 * Contains:
 *  - the immunization itself
 *  - the practitioner who performed it
 *  - observations related to this immunization
 */
public class ImmunizationBlockDTO {

    private ImmunizationDTO immunization;          /// Main immunization data
    private PractitionerDTO practitioner;          /// peactitioner -  who performed / recorded it
    private List<ObservationDTO> observations;     /// Observations about the immunization procedure

    /// --- getters & setters ---

    public ImmunizationDTO getImmunization() {
        return immunization;
    }

    public void setImmunization(ImmunizationDTO immunization) {
        this.immunization = immunization;
    }

    public PractitionerDTO getPractitioner() {
        return practitioner;
    }

    public void setPractitioner(PractitionerDTO practitioner) {
        this.practitioner = practitioner;
    }

    public List<ObservationDTO> getObservations() {
        return observations;
    }

    public void setObservations(List<ObservationDTO> observations) {
        this.observations = observations;
    }
}
