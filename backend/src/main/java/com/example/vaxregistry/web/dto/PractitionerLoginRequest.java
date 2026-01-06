package com.example.vaxregistry.web.dto;

/**
 * Testing-only doctor login request.
 *
 * Provide either {@code identifier} (preferred) or {@code name}.
 */
public class PractitionerLoginRequest {
    private String identifier;
    private String name;

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
}
