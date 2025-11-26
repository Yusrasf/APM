package org.apm.backend.dto.practitioner;

/**
 * DTO used to display basic Location information
 */
public class LocationDTO {

    private String locationId; /// FHIR Location.id (technical id)
    private String name; /// Display name of the location
    private String addressLine;

    /// --- getters & setters ---
    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

}
