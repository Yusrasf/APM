package org.apm.backend.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.annotation.WebServlet;
import org.apm.backend.fhir.provider.PractitionerResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HAPI FHIR server configuration (server-side).
 *
 * This class exposes the HAPI {@link RestfulServer} as a servlet under
 * the URL path {@code /fhir/*}. It registers the {@link PractitionerResourceProvider}
 * so that FHIR Practitioner resources can be handled by this server.

 * The servlet is discovered by the container via the {@link WebServlet}
 * annotation, and the class is also a Spring bean via {@link Component}
 * so that Spring can inject the resource provider.
 */

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
@Component
public class FHIRServerConfig extends RestfulServer {

    private final PractitionerResourceProvider practitionerResourceProvider;

    @Autowired
    public FHIRServerConfig(PractitionerResourceProvider practitionerResourceProvider) {
        this.practitionerResourceProvider = practitionerResourceProvider;
    }
/**
 * Initializes the HAPI FHIR RestfulServer.
 *
 * <p>
 * This method is called by the HAPI framework when the servlet starts.
 **/

    @Override
    protected void initialize() {
        /// Use FHIR R5
        setFhirContext(FhirContext.forR5());

        /// Register thr Practitioner provider
        registerProvider(practitionerResourceProvider);

        /// JSON in responses
        setDefaultPrettyPrint(true);

        System.out.println("DEBUG: FHIR server initialized! Registered Providers: Practitioner");
    }
}
