package org.apm.backend.fhir.provider;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Practitioner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class PractitionerResourceProvider implements IResourceProvider {

    private final Map<String, Practitioner> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1L);

    @Override
    public Class<Practitioner> getResourceType() {
        return Practitioner.class;
    }

    @Create
    public MethodOutcome create(@ResourceParam Practitioner practitioner) {
        String idPart = String.valueOf(idCounter.getAndIncrement());
        IdType id = new IdType("Practitioner", idPart);
        practitioner.setId(id);

        store.put(idPart, practitioner);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(id);
        outcome.setResource(practitioner);
        return outcome;
    }

    @Read
    public Practitioner read(@IdParam IdType id) {
        String idPart = id.getIdPart();
        Practitioner practitioner = store.get(idPart);
        if (practitioner == null) {
            throw new ResourceNotFoundException("Practitioner/" + idPart + " is not known");
        }
        return practitioner;
    }

    /**
     * Simple search by identifier, e.g.:
     * GET /fhir/Practitioner?identifier=DOC123
     */
    @Search
    public List<Practitioner> searchByIdentifier(
            @ca.uhn.fhir.rest.annotation.RequiredParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier) {

        if (identifier == null || identifier.isEmpty() || identifier.getValue() == null) {
            return Collections.emptyList();
        }

        String valueToMatch = identifier.getValue();

        return store.values().stream()
                .filter(p -> p.hasIdentifier()
                        && valueToMatch.equals(p.getIdentifierFirstRep().getValue()))
                .collect(Collectors.toList());
    }
}
