package com.example.vaxregistry.model;

/**
 * Minimal request body used by the demo UI to create Patient resources.
 *
 * The configured FHIR server is the system of record.
 */
public class PatientCreateRequest {

    private String givenName;
    private String familyName;

    /** yyyy-MM-dd (optional) */
    private String birthDate;

    /** male|female|other|unknown (optional) */
    private String gender;

    /** Optional identifier token components */
    private String identifierSystem;
    private String identifierValue;

    /** Optional country label (e.g., AT) */
    private String country;

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdentifierSystem() {
        return identifierSystem;
    }

    public void setIdentifierSystem(String identifierSystem) {
        this.identifierSystem = identifierSystem;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
