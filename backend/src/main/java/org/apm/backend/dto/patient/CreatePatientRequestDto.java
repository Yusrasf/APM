package org.apm.backend.dto.patient;

public class CreatePatientRequestDto {
    private String firstName;
    private String lastName;
    private String identifier;
    private String birthDate;
    private String relatedPersonIdentifier;
    /// Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getrelatedPersonIdentifier() {
        return relatedPersonIdentifier;
    }
    /// Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setRelatedPersonIdentifier(String relatedPersonIdentifier) {
        this.relatedPersonIdentifier = relatedPersonIdentifier;
    }

}
