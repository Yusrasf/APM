package org.apm.backend.entity.practitioner;

import jakarta.persistence.*;

@Entity
@Table(name = "practitioners")
public class PractitionerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique login identifier for the practitioner (e.g. DOC123, SSN, etc.).
     */
    @Column(name = "identifier", nullable = false, unique = true)
    private String identifier;

    /**
     * Hashed password (e.g. BCrypt hash).
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "organization_name")
    private String organizationName;

    public PractitionerEntity() {
    }

    // --- Getters and setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

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

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
