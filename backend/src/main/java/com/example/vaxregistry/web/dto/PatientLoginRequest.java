package com.example.vaxregistry.web.dto;

/**
 * Testing-only login request.
 *
 * Provide either {@code identifier} (preferred) or {@code name}.
 * If {@code birthDate} is provided, results are filtered/verified against it.
 */
public class PatientLoginRequest {
    private String identifier;
    private String name;
    private String birthDate;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
