package org.apm.backend.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.annotation.WebServlet;
import org.apm.backend.fhir.provider.PatientResourceProvider; ///replace, add
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
@Component
public class FHIRServerConfig extends RestfulServer{
    @Autowired
    private PatientResourceProvider patientResourceProvider;


    @Override
    protected void initialize() {

        // Select FHIR version
        setFhirContext(FhirContext.forR5());

        // Register resource providers
        registerProvider(patientResourceProvider);


        // Pretty JSON output
        setDefaultPrettyPrint(true);

@Configuration
public class FHIRServerConfig {

    @Bean
    public FhirContext fhirContext() {
        // R5 context
        return FhirContext.forR5();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        // TODO: change to the URL of your HAPI FHIR server
        String baseUrl = "http://localhost:8080/fhir";
        return fhirContext.newRestfulGenericClient(baseUrl);
    }
}
