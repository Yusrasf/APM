package org.apm.backend.dto     .consent;

public class ConsentDto {

    private String id;
    private String patientId;

    private String status; // e.g. "active", "inactive"
    private boolean shareWithPhysicians;
    private boolean shareWithPublicHealth;

    private String lastUpdated;

    public ConsentDto() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isShareWithPhysicians() {
        return shareWithPhysicians;
    }

    public void setShareWithPhysicians(boolean shareWithPhysicians) {
        this.shareWithPhysicians = shareWithPhysicians;
    }

    public boolean isShareWithPublicHealth() {
        return shareWithPublicHealth;
    }

    public void setShareWithPublicHealth(boolean shareWithPublicHealth) {
        this.shareWithPublicHealth = shareWithPublicHealth;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
