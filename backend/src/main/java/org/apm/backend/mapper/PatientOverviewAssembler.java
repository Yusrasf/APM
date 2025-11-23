package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.EncounterBlockDTO;
import org.apm.backend.dto.practitioner.ImmunizationBlockDTO;
import org.apm.backend.dto.practitioner.PatientClinicalOverviewDTO;
import org.apm.backend.mapper.*;
import org.hl7.fhir.r5.model.*;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PatientOverviewAssembler {

    private final PatientMapper patientMapper;
    private final EncounterMapper encounterMapper;
    private final ImmunizationMapper immunizationMapper;
    private final LocationMapper locationMapper;
    private final OrganizationMapper organizationMapper;
    private final PractitionerMapper practitionerMapper;
    private final ObservationMapper observationMapper;

    public PatientOverviewAssembler(PatientMapper patientMapper,
                                    EncounterMapper encounterMapper,
                                    ImmunizationMapper immunizationMapper,
                                    LocationMapper locationMapper,
                                    OrganizationMapper organizationMapper,
                                    PractitionerMapper practitionerMapper,
                                    ObservationMapper observationMapper) {
        this.patientMapper = patientMapper;
        this.encounterMapper = encounterMapper;
        this.immunizationMapper = immunizationMapper;
        this.locationMapper = locationMapper;
        this.organizationMapper = organizationMapper;
        this.practitionerMapper = practitionerMapper;
        this.observationMapper = observationMapper;
    }

    public PatientClinicalOverviewDTO toOverview(
            Patient patient,
            List<Encounter> encounters,
            Map<String, Location> locationById,
            Map<String, Organization> orgById,
            Map<String, List<Immunization>> immByEncounterId,
            Map<String, Practitioner> practitionerById,
            Map<String, Medication> medicationById,
            Map<String, List<Observation>> obsByImmunizationId) {

        PatientClinicalOverviewDTO dto = new PatientClinicalOverviewDTO();

        // patient box
        dto.setPatient(patientMapper.toPatientDetailsDTO(patient));

        // encounter blocks
        List<EncounterBlockDTO> encounterBlocks = encounters.stream()
                .map(encounter -> {
                    EncounterBlockDTO block = new EncounterBlockDTO();

                    block.setEncounter(encounterMapper.toEncounterDTO(encounter));

                    String encounterId = encounter.getIdElement().getIdPart();

                    // location
                    Location loc = locationById.get(encounterId);
                    if (loc != null) {
                        block.setLocation(locationMapper.toLocationDTO(loc));
                    }

                    // organization
                    Organization org = orgById.get(encounterId);
                    if (org != null) {
                        block.setOrganization(organizationMapper.toOrganizationDTO(org));
                    }

                    // immunizations in this encounter
                    List<Immunization> imms = immByEncounterId.getOrDefault(encounterId, List.of());
                    List<ImmunizationBlockDTO> immBlocks = imms.stream()
                            .map(imm -> {
                                ImmunizationBlockDTO ib = new ImmunizationBlockDTO();
                                ib.setImmunization(immunizationMapper.toImmunizationDTO(imm));

                                // practitioner
                                Practitioner prac = /* find practitioner for this imm from practitionerById */;
                                if (prac != null) {
                                    ib.setPractitioner(practitionerMapper.toPractitionerDTO(prac));
                                }

                                // observations
                                List<Observation> obsList =
                                        obsByImmunizationId.getOrDefault(
                                                imm.getIdElement().getIdPart(), List.of());
                                ib.setObservations(
                                        obsList.stream()
                                                .map(observationMapper::toObservationDTO)
                                                .toList()
                                );

                                return ib;
                            })
                            .toList();

                    block.setImmunizations(immBlocks);

                    return block;
                })
                .toList();

        dto.setEncounters(encounterBlocks);
        return dto;
    }
}
