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

    // small mappers for the simple endpoints
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

    // -------------------------------------------------------------------------
    // 1) searchPatientsByIdentifier  (used by /patients/searchByIdentifier)
    // -------------------------------------------------------------------------
    public List<PatientDetailsDTO> searchPatientsByIdentifier(String identifier) {

        Bundle bundle = fhirClient
                .search()
                .forResource(Patient.class)
                // search by Patient.identifier value
                .where(new TokenClientParam("identifier").exactly().code(identifier))
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(e -> (Patient) e.getResource())
                .map(patientMapper::toPatientDetailsDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 2) getEncountersForPatient  (used by /patients/{patientId}/encounters)
    // -------------------------------------------------------------------------
    public List<EncounterDTO> getEncountersForPatient(String patientId) {

        Bundle encBundle = fhirClient
                .search()
                .forResource(Encounter.class)
                // subject = Patient/{patientId}
                .where(new ReferenceClientParam("subject").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        return encBundle.getEntry().stream()
                .map(e -> (Encounter) e.getResource())
                .map(encounterMapper::toEncounterDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 3) getImmunizationsForEncounter (used by /encounters/{encounterId}/immunizations)
    // -------------------------------------------------------------------------
    public List<ImmunizationDTO> getImmunizationsForEncounter(String encounterId) {

        Bundle immBundle = fhirClient
                .search()
                .forResource(Immunization.class)
                // encounter = Encounter/{encounterId}
                .where(new ReferenceClientParam("encounter").hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();

        return immBundle.getEntry().stream()
                .map(e -> (Immunization) e.getResource())
                .map(immunizationMapper::toImmunizationDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 4) getClinicalOverview (used by /patients/{patientId}/clinical-overview)
    // -------------------------------------------------------------------------
    public PatientClinicalOverviewDTO getClinicalOverview(String patientId) {
        return buildOverviewForPatient(patientId);
    }

    // ===== internal helper: this is the big logic we wrote earlier ==========
    private PatientClinicalOverviewDTO buildOverviewForPatient(String patientId) {

        // 1) Load Patient
        Patient patient = fhirClient
                .read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();

        // 2) Load Encounters for this Patient: subject=Patient/{id}
        Bundle encBundle = fhirClient
                .search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("subject").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<Encounter> encounters = encBundle.getEntry().stream()
                .map(e -> (Encounter) e.getResource())
                .collect(Collectors.toList());

        Map<String, Location> locationByEncounterId = new HashMap<>();
        Map<String, Organization> orgByEncounterId = new HashMap<>();
        Map<String, List<Immunization>> immByEncounterId = new HashMap<>();
        Map<String, Practitioner> practitionerByImmId = new HashMap<>();
        Map<String, List<Observation>> obsByImmunizationId = new HashMap<>();

        // 3) For each encounter, load related resources
        for (Encounter enc : encounters) {
            String encId = enc.getIdElement().getIdPart();

            // --- Location from Encounter.location[0].location ---
            if (enc.hasLocation()
                    && enc.getLocationFirstRep().hasLocation()
                    && enc.getLocationFirstRep().getLocation().getReferenceElement().hasIdPart()) {

                String locId = enc.getLocationFirstRep()
                        .getLocation()
                        .getReferenceElement()
                        .getIdPart();

                Location loc = fhirClient
                        .read()
                        .resource(Location.class)
                        .withId(locId)
                        .execute();

                locationByEncounterId.put(encId, loc);
            }

            // --- Organization from Encounter.serviceProvider ---
            if (enc.hasServiceProvider()
                    && enc.getServiceProvider().getReferenceElement().hasIdPart()) {

                String orgId = enc.getServiceProvider()
                        .getReferenceElement()
                        .getIdPart();

                Organization org = fhirClient
                        .read()
                        .resource(Organization.class)
                        .withId(orgId)
                        .execute();

                orgByEncounterId.put(encId, org);
            }

            // --- Immunizations in this encounter: encounter=Encounter/{encId} ---
            Bundle immBundle = fhirClient
                    .search()
                    .forResource(Immunization.class)
                    .where(new ReferenceClientParam("encounter").hasId(encId))
                    .returnBundle(Bundle.class)
                    .execute();

            List<Immunization> imms = immBundle.getEntry().stream()
                    .map(e -> (Immunization) e.getResource())
                    .collect(Collectors.toList());

            immByEncounterId.put(encId, imms);

            // For each immunization: practitioner + observations
            for (Immunization imm : imms) {
                String immId = imm.getIdElement().getIdPart();

                // Practitioner from Immunization.performer[0].actor
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

                // Observations linked to this immunization via focus=Immunization/{id}
                Bundle obsBundle = fhirClient
                        .search()
                        .forResource(Observation.class)
                        .where(new ReferenceClientParam("focus")
                                .hasId("Immunization/" + immId))
                        .returnBundle(Bundle.class)
                        .execute();

                List<Observation> obsList = obsBundle.getEntry().stream()
                        .map(e -> (Observation) e.getResource())
                        .collect(Collectors.toList());

                obsByImmunizationId.put(immId, obsList);
            }
        }

        // 4) Assemble DTO
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
