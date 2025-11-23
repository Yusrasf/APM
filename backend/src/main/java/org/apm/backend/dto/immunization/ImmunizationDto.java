package org.apm.backend.dto.immunization;

public class ImmunizationDto {

    private String id;
    private String patientId;

    private String vaccineSystem;
    private String vaccineCode;
    private String vaccineDisplay;

    private String status;
    private String occurrenceDate;
    private String lotNumber;

    private String locationDisplay;
    private String performerDisplay;

    private boolean selfReported;

    public ImmunizationDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getPerformerDisplay() {
        return performerDisplay;
    }

    public void setPerformerDisplay(String performerDisplay) {
        this.performerDisplay = performerDisplay;
    }

    public boolean isSelfReported() {
        return selfReported;
    }

    public void setSelfReported(boolean selfReported) {
        this.selfReported = selfReported;
    }
}
