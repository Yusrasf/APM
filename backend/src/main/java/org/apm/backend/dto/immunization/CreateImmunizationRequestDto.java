package org.apm.backend.dto.immunization;

public class CreateImmunizationRequestDto {

    private String vaccineSystem;
    private String vaccineCode;
    private String vaccineDisplay;

    /**
     * Date of vaccination (ISO-8601 yyyy-MM-dd).
     */
    private String occurrenceDate;

    private String lotNumber;

    /**
     * Free-text description of where it was done
     * (e.g. "Hausarztpraxis Dr. Müller").
     */
    private String locationDisplay;

    /**
     * Whether this was self-reported by the patient.
     * For the patient app this will usually be true.
     */
    private boolean selfReported = true;

    public CreateImmunizationRequestDto() {
    }

    public String getVaccineSystem() {
        return vaccineSystem;
    }

    public void setVaccineSystem(String vaccineSystem) {
        this.vaccineSystem = vaccineSystem;
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

    public String getOccurrenceDate() {
        return occurrenceDate;
    }

    public void setOccurrenceDate(String occurrenceDate) {
        this.occurrenceDate = occurrenceDate;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getLocationDisplay() {
        return locationDisplay;
    }

    public void setLocationDisplay(String locationDisplay) {
        this.locationDisplay = locationDisplay;
    }

    public boolean isSelfReported() {
        return selfReported;
    }

    public void setSelfReported(boolean selfReported) {
        this.selfReported = selfReported;
    }
}
