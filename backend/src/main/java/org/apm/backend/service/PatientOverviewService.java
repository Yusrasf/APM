package org.apm.backend.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import org.apm.backend.dto.practitioner.EncounterDTO;
import org.apm.backend.dto.practitioner.ImmunizationDTO;
import org.apm.backend.dto.practitioner.PatientClinicalOverviewDTO;
import org.apm.backend.dto.practitioner.PatientDetailsDTO;
import org.apm.backend.mapper.EncounterMapper;
import org.apm.backend.mapper.ImmunizationMapper;
import org.apm.backend.mapper.PatientMapper;
import org.apm.backend.mapper.PatientOverviewAssembler;
import org.hl7.fhir.r5.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientOverviewService {

    private final IGenericClient fhirClient;
    private final PatientOverviewAssembler overviewAssembler;

    private final PatientMapper patientMapper;
    private final EncounterMapper encounterMapper;
    private final ImmunizationMapper immunizationMapper;

    public PatientOverviewService(IGenericClient fhirClient,
                                  PatientOverviewAssembler overviewAssembler,
                                  PatientMapper patientMapper,
                                  EncounterMapper encounterMapper,
                                  ImmunizationMapper immunizationMapper) {

        this.fhirClient = fhirClient;
        this.overviewAssembler = overviewAssembler;
        this.patientMapper = patientMapper;
        this.encounterMapper = encounterMapper;
        this.immunizationMapper = immunizationMapper;
    }

    // ------------------------------------------------------------
    // 1) Search patients by identifier
    // ------------------------------------------------------------
    public List<PatientDetailsDTO> searchPatientsByIdentifier(String identifier) {

        Bundle bundle = fhirClient
                .search()
                .forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().code(identifier))
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(e -> (Patient) e.getResource())
                .map(patientMapper::toPatientDetailsDTO)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------
    // 2) Encounters for patient
    // ------------------------------------------------------------
    public List<EncounterDTO> getEncountersForPatient(String patientId) {

        Bundle encBundle = fhirClient
                .search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("subject").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        return encBundle.getEntry().stream()
                .map(e -> (Encounter) e.getResource())
                .map(encounterMapper::toEncounterDTO)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------
    // 3) Immunizations for encounter
    // ------------------------------------------------------------
    public List<ImmunizationDTO> getImmunizationsForEncounter(String encounterId) {

        // MUST LOAD BY PATIENT THEN FILTER — encounter is NOT a search param in R5

        // Extract patient ID from encounterId manually not possible, backend endpoint already passes encounterId
        // So load encounter to get patient
        Encounter encounter = fhirClient
                .read()
                .resource(Encounter.class)
                .withId(encounterId)
                .execute();

        String patientId = encounter.getSubject().getReferenceElement().getIdPart();

        // Search ALL immunizations for the patient
        Bundle immBundle = fhirClient
                .search()
                .forResource(Immunization.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<Immunization> allImms = immBundle.getEntry().stream()
                .map(e -> (Immunization) e.getResource())
                .collect(Collectors.toList());

        // Filter only immunizations belonging to THIS encounter
        List<Immunization> belonging = allImms.stream()
                .filter(i -> i.hasEncounter()
                        && i.getEncounter().getReferenceElement().hasIdPart()
                        && i.getEncounter().getReferenceElement().getIdPart().equals(encounterId))
                .collect(Collectors.toList());

        return belonging.stream()
                .map(immunizationMapper::toImmunizationDTO)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------
    // 4) Clinical overview
    // ------------------------------------------------------------
    public PatientClinicalOverviewDTO getClinicalOverview(String patientId) {
        return buildOverviewForPatient(patientId);
    }

    private PatientClinicalOverviewDTO buildOverviewForPatient(String patientId) {

        // Load patient
        Patient patient = fhirClient
                .read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();

        // Load encounters
        Bundle encBundle = fhirClient
                .search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("subject").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<Encounter> encounters = encBundle.getEntry().stream()
                .map(e -> (Encounter) e.getResource())
                .collect(Collectors.toList());

        // Load ALL immunizations for patient once
        Bundle immBundle = fhirClient
                .search()
                .forResource(Immunization.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<Immunization> allImmunizations = immBundle.getEntry().stream()
                .map(e -> (Immunization) e.getResource())
                .collect(Collectors.toList());

        // Maps
        Map<String, Location> locationByEncounterId = new HashMap<>();
        Map<String, Organization> orgByEncounterId = new HashMap<>();
        Map<String, List<Immunization>> immByEncounterId = new HashMap<>();
        Map<String, Practitioner> practitionerByImmId = new HashMap<>();
        Map<String, List<Observation>> obsByImmunizationId = new HashMap<>();

        // Process encounters
        for (Encounter enc : encounters) {

            String encId = enc.getIdElement().getIdPart();

            // --- Location ---
            if (enc.hasLocation()
                    && enc.getLocationFirstRep().hasLocation()
                    && enc.getLocationFirstRep().getLocation().getReferenceElement().hasIdPart()) {

                String locId = enc.getLocationFirstRep().getLocation().getReferenceElement().getIdPart();

                Location loc = fhirClient
                        .read()
                        .resource(Location.class)
                        .withId(locId)
                        .execute();

                locationByEncounterId.put(encId, loc);
            }

            // --- Organization ---
            if (enc.hasServiceProvider()
                    && enc.getServiceProvider().getReferenceElement().hasIdPart()) {

                String orgId = enc.getServiceProvider().getReferenceElement().getIdPart();

                Organization org = fhirClient
                        .read()
                        .resource(Organization.class)
                        .withId(orgId)
                        .execute();

                orgByEncounterId.put(encId, org);
            }

            // --- Immunizations for this encounter (FILTER) ---
            List<Immunization> imms = allImmunizations.stream()
                    .filter(i -> i.hasEncounter()
                            && i.getEncounter().getReferenceElement().hasIdPart()
                            && i.getEncounter().getReferenceElement().getIdPart().equals(encId))
                    .collect(Collectors.toList());

            immByEncounterId.put(encId, imms);

            // Practitioner + Observations
            for (Immunization imm : imms) {

                String immId = imm.getIdElement().getIdPart();

                // Practitioner
                if (imm.hasPerformer()
                        && imm.getPerformerFirstRep().hasActor()
                        && imm.getPerformerFirstRep().getActor().getReferenceElement().hasIdPart()) {

                    String pracId = imm.getPerformerFirstRep()
                            .getActor()
                            .getReferenceElement()
                            .getIdPart();

                    Practitioner prac = fhirClient
                            .read()
                            .resource(Practitioner.class)
                            .withId(pracId)
                            .execute();

                    practitionerByImmId.put(immId, prac);
                }

                // Observations (R5 does NOT support focus search → filter manually)
                Bundle obsBundle = fhirClient
                        .search()
                        .forResource(Observation.class)
                        .where(new ReferenceClientParam("subject").hasId(patientId))
                        .returnBundle(Bundle.class)
                        .execute();

                List<Observation> obsFiltered = obsBundle.getEntry().stream()
                        .map(e -> (Observation) e.getResource())
                        .filter(o -> o.getFocus().stream().anyMatch(ref ->
                                ref.getReferenceElement().hasIdPart()
                                        && ref.getReferenceElement().getIdPart().equals(immId)))
                        .collect(Collectors.toList());

                obsByImmunizationId.put(immId, obsFiltered);
            }
        }

        return overviewAssembler.toOverview(
                patient,
                encounters,
                locationByEncounterId,
                orgByEncounterId,
                immByEncounterId,
                practitionerByImmId,
                obsByImmunizationId
        );
    }
}
