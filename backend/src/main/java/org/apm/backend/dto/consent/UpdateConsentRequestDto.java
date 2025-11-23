package org.apm.backend.dto.consent;

public class UpdateConsentRequestDto {

    private boolean shareWithPhysicians;
    private boolean shareWithPublicHealth;

    public UpdateConsentRequestDto() {
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
}
