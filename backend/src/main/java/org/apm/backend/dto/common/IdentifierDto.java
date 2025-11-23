package org.apm.backend.dto.common;

public class IdentifierDto {

    private String system;
    private String value;

    public IdentifierDto() {
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
