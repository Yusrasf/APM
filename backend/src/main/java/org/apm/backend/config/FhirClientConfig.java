package org.apm.backend.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirClientConfig {

    @Bean
    public FhirContext fhirContext() {
        // Use FHIR R5
        return FhirContext.forR5();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        // Use the port where your app runs: 8081 or 8080
        String baseUrl = "http://localhost:8081/fhir";
        return fhirContext.newRestfulGenericClient(baseUrl);
    }
}
