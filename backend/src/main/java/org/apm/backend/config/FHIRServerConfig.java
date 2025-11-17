package org.apm.backend.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.annotation.WebServlet;
import org.apm.backend.fhir.PatientResourceProvider; ///replace, add
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

        // (Optional) Turn off strict validation
        // getFhirContext().getRestfulClientFactory().setServerValidationMode(null);

        System.out.println("DEBUG: FHIR server initialized!");
    }

}
