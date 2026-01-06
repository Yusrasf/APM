package com.example.vaxregistry.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Minimal request body used by the demo UI to create Immunization resources.
 * You can also bypass this and use the generic FHIR CRUD endpoints with full FHIR JSON.
 */
public class ImmunizationCreateRequest {

    @NotBlank
    private String vaccineCode;

    private String vaccineSystem = "http://hl7.org/fhir/sid/cvx";
    private String vaccineDisplay;

    /** ISO 8601 date or dateTime, e.g. 2025-01-31 or 2025-01-31T10:30:00+01:00 */
    @NotBlank
    private String occurrenceDateTime;

    private String lotNumber;
    private String note;

    /** Optional FHIR IDs */
    private String performerPractitionerId;
    private String organizationId;
    private String locationId;

    public String getVaccineCode() {
        return vaccineCode;
    }

    public void setVaccineCode(String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    public String getVaccineSystem() {
        return vaccineSystem;
    }

    public void setVaccineSystem(String vaccineSystem) {
        this.vaccineSystem = vaccineSystem;
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

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPerformerPractitionerId() {
        return performerPractitionerId;
    }

    public void setPerformerPractitionerId(String performerPractitionerId) {
        this.performerPractitionerId = performerPractitionerId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
}
