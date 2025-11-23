package org.apm.backend.fhir.provider;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component // Read, Create, Search (by name, by identifier)
public class PatientResourceProvider implements IResourceProvider {

    private final Map<String, Patient> patientStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1L);

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Create
    public MethodOutcome create(@ResourceParam Patient patient) {
        String idPart = String.valueOf(idCounter.getAndIncrement());
        IdType id = new IdType("Patient", idPart);
        patient.setId(id);

        patientStore.put(idPart, patient);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(id);
        outcome.setResource(patient);
        return outcome;
    }

    @Read
    public Patient read(@IdParam IdType id) {
        String idPart = id.getIdPart();
        Patient patient = patientStore.get(idPart);
        if (patient == null) {
            throw new ResourceNotFoundException("Patient/" + idPart + " is not known");
        }
        return patient;
    }

    /**
     * SEARCH Patient by name (given or family). If name not provided, return all patients.
     */
    @Search
    public List<Patient> searchByName(
            @OptionalParam(name = Patient.SP_NAME) StringParam name) {

        if (name == null || name.isEmpty()) {
            return new ArrayList<>(patientStore.values());
        }

        String searchValue = name.getValue().toLowerCase();

        return patientStore.values().stream()
                .filter(patient ->
                        patient.getNameFirstRep()
                                .getNameAsSingleString()
                                .toLowerCase()
                                .contains(searchValue))
                .collect(Collectors.toList());
    }

    /**
     * SEARCH Patient by business identifier.
     */
    @Search
    public List<Patient> searchByIdentifier(
            @RequiredParam(name = Patient.SP_IDENTIFIER) TokenParam identifier) {

        if (identifier == null || identifier.isEmpty()
                || identifier.getValue() == null) {
            return new ArrayList<>();
        }

        String valueToMatch = identifier.getValue();

        return patientStore.values().stream()
                .filter(patient ->
                        patient.hasIdentifier()
                                && valueToMatch.equals(
                                patient.getIdentifierFirstRep().getValue()))
                .collect(Collectors.toList());
    }
}
