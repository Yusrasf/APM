package org.apm.backend.dto.practitioner;

/**
 * DTO used to display basic Organization information
 * ( hospital / clinic).
 */
public class OrganizationDTO {

    private String organizationId; /// FHIR Organization.id (technical id)
    private String name;           /// Display name of the organization (hospital name)

    /// --- getters & setters ---
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
