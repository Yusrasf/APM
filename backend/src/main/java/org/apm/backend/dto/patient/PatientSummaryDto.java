package org.apm.backend.dto.patient;

public class PatientSummaryDto {
    private String id;
    private String fullName;
    private String birthDate;
    private String identifier;
/// Getters and setters
    public PatientSummaryDto() {
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}