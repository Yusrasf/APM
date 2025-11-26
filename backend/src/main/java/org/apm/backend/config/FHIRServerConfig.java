package org.apm.backend.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.annotation.WebServlet;
import org.apm.backend.fhir.provider.PractitionerResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
@Component
public class FHIRServerConfig extends RestfulServer {

    private final PractitionerResourceProvider practitionerResourceProvider;

    @Autowired
    public FHIRServerConfig(PractitionerResourceProvider practitionerResourceProvider) {
        this.practitionerResourceProvider = practitionerResourceProvider;
    }

    @Override
    protected void initialize() {
        // Use FHIR R5
        setFhirContext(FhirContext.forR5());

        // Register your Practitioner provider
        registerProvider(practitionerResourceProvider);

        // Pretty JSON in responses
        setDefaultPrettyPrint(true);

        System.out.println("DEBUG: FHIR server initialized! Registered Providers: Practitioner");
    }
}
