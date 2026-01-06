package com.example.vaxregistry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ice")
public class IceProperties {

    /** DSS endpoint URL (e.g. http://host/opencds-decision-support-service/evaluate). */
    private String endpointUrl;

    private Km km = new Km();

    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 30_000;

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public Km getKm() {
        return km;
    }

    public void setKm(Km km) {
        this.km = km;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public static class Km {
        private String scopingEntityId = "org.nyc.cir";
        private String businessId = "ICE";
        private String version = "1.0.0";

        public String getScopingEntityId() {
            return scopingEntityId;
        }

        public void setScopingEntityId(String scopingEntityId) {
            this.scopingEntityId = scopingEntityId;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
