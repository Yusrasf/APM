package org.apm.backend.dto.practitioner;

public class RegistrationResponseDTO {

    private String message;

    public RegistrationResponseDTO() {}

    public RegistrationResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

