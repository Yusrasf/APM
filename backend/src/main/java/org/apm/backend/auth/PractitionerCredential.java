package org.apm.backend.auth;

public class PractitionerCredential {

    private String identifier;
    private String password;   // <--- IMPORTANT: must be "password"

    public PractitionerCredential() {
    }

    public PractitionerCredential(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {   // <--- AuthServiceImpl calls this
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
