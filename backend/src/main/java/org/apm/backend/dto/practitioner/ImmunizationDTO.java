package org.apm.backend.dto.practitioner;

/**
 * DTO used to display basic Immunization information.
 */
public class ImmunizationDTO {

    private String immunizationId;   /// FHIR Immunization.id (technical id)
    private String patientId;        /// FHIR Patient.id this immunization belongs to
    private String encounterId;      /// FHIR Encounter.id in which it was given
    private String organizationId;   /// FHIR Organization.id (hospital/clinic that provided it)

    private String vaccineCode;      /// Code of the vaccine (CVX)
    private String vaccineDisplay;   /// Human-readable vaccine name

    private String occurrenceDateTime; /// When the dose was given
    private String status;             /// completed | entered-in-error | not-done | etc.
    private String lotNumber;          /// Vaccine lot/batch number (optional)

    /// --- getters & setters ---

    public String getImmunizationId() {
        return immunizationId;
    }

    public void setImmunizationId(String immunizationId) {
        this.immunizationId = immunizationId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getVaccineCode() {
        return vaccineCode;
    }

    public void setVaccineCode(String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    public String getVaccineDisplay() {
        return vaccineDisplay;
    }

    public void setVaccineDisplay(String vaccineDisplay) {
        this.vaccineDisplay = vaccineDisplay;
    }

    public String getOccurrenceDateTime() {
        return occurrenceDateTime;
    }

    public void setOccurrenceDateTime(String occurrenceDateTime) {
        this.occurrenceDateTime = occurrenceDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }
}
