package org.apm.backend.fhir;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam; ///used to search by identifier
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Patient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component /// Read, Create, Search (by name), search (by identifier)
public class PatientResourceProvider {
    private final Map<String, Patient> patientStore = new HashMap<>();
    private int counter = 1;
    /** READ Patient by ID */
    @Read
    public Patient read(@IdParam IdType id) {
        String idPart = id.getIdPart();

        if (!patientStore.containsKey(idPart)) {
            throw new ResourceNotFoundException("Patient with ID " + idPart + " not found");
        }
        return patientStore.get(idPart);
    }
    /** CREATE Patient */
    @Create
    public MethodOutcome create(@ResourceParam Patient patient) {
        String newId = String.valueOf(counter++);  ///generate new ID
        patient.setId(newId);
        patientStore.put(newId, patient); /// Store patient

        /// Return outcome with ID
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("Patient", newId));
        outcome.setResource(patient);
        return outcome;
    }
    /** SEARCH Patient by name */
    @Search
    public List<Patient> searchByName(@OptionalParam(name = Patient.SP_NAME) StringParam name) {

        if (name == null) {
            return new ArrayList<>(patientStore.values());
        }

        String searchValue = name.getValue().toLowerCase();

        return patientStore.values().stream().filter(patient -> patient.getNameFirstRep().getNameAsSingleString().toLowerCase().contains(searchValue))
                .collect(Collectors.toList());
    }

    /** SEARCH Patient by business identifier  */

}
