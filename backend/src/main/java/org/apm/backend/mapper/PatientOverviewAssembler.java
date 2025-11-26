package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.EncounterBlockDTO;
import org.apm.backend.dto.practitioner.ImmunizationBlockDTO;
import org.apm.backend.dto.practitioner.PatientClinicalOverviewDTO;
import org.apm.backend.dto.practitioner.ImmunizationDTO;
import org.apm.backend.dto.practitioner.PractitionerDTO;
import org.apm.backend.dto.practitioner.ObservationDTO;
import org.apm.backend.dto.practitioner.EncounterDTO;
import org.apm.backend.dto.practitioner.LocationDTO;
import org.apm.backend.dto.practitioner.OrganizationDTO;
import org.apm.backend.dto.practitioner.PatientDetailsDTO;

import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Immunization;
import org.hl7.fhir.r5.model.Location;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Organization;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Practitioner;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Assembles the full {@link PatientClinicalOverviewDTO} from FHIR resources.
 * This component combines:
 * <ul>
 *     <li>Patient</li>
 *     <li>Encounters</li>
 *     <li>Locations &amp; Organizations per encounter</li>
 *     <li>Immunizations per encounter</li>
 *     <li>Practitioner and Observations per immunization</li>
 * </ul>
 * into the hierarchical DTO structure used by the practitioner UI.
 */

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

    /**
     * Build the big screen DTO from all FHIR resources.
     *
     * @param locationById            key: encounterId  -> Location
     * @param orgById                 key: encounterId  -> Organization
     * @param immByEncounterId        key: encounterId  -> list of Immunizations
     * @param practitionerByImmId     key: immunizationId -> Practitioner
     * @param obsByImmunizationId     key: immunizationId -> list of Observations
     */
    public PatientClinicalOverviewDTO toOverview(
            Patient patient,
            List<Encounter> encounters,
            Map<String, Location> locationById,
            Map<String, Organization> orgById,
            Map<String, List<Immunization>> immByEncounterId,
            Map<String, Practitioner> practitionerByImmId,
            Map<String, List<Observation>> obsByImmunizationId) {

        PatientClinicalOverviewDTO dto = new PatientClinicalOverviewDTO();

        // patient box
        PatientDetailsDTO patientDetails = patientMapper.toPatientDetailsDTO(patient);
        dto.setPatient(patientDetails);

        // encounter blocks
        List<EncounterBlockDTO> encounterBlocks = encounters.stream()
                .map(encounter -> {
                    EncounterBlockDTO block = new EncounterBlockDTO();

                    // encounter
                    EncounterDTO encounterDTO = encounterMapper.toEncounterDTO(encounter);
                    block.setEncounter(encounterDTO);

                    String encounterId = encounter.getIdElement().getIdPart();

                    // location
                    Location loc = locationById.get(encounterId);
                    if (loc != null) {
                        LocationDTO locDto = locationMapper.toLocationDTO(loc);
                        block.setLocation(locDto);
                    }

                    // organization
                    Organization org = orgById.get(encounterId);
                    if (org != null) {
                        OrganizationDTO orgDto = organizationMapper.toOrganizationDTO(org);
                        block.setOrganization(orgDto);
                    }

                    // immunizations in this encounter
                    List<Immunization> imms =
                            immByEncounterId.getOrDefault(encounterId, List.of());

                    List<ImmunizationBlockDTO> immBlocks = imms.stream()
                            .map(imm -> {
                                ImmunizationBlockDTO ib = new ImmunizationBlockDTO();

                                // immunization main data
                                ImmunizationDTO immDto = immunizationMapper.toImmunizationDTO(imm);
                                ib.setImmunization(immDto);

                                String immId = imm.getIdElement().getIdPart();

                                // practitioner (if provided in map)
                                Practitioner prac = practitionerByImmId != null
                                        ? practitionerByImmId.get(immId)
                                        : null;
                                if (prac != null) {
                                    PractitionerDTO pracDto = practitionerMapper.toPractitionerDTO(prac);
                                    ib.setPractitioner(pracDto);
                                }

                                // observations for this immunization
                                List<Observation> obsList =
                                        obsByImmunizationId.getOrDefault(immId, List.of());

                                List<ObservationDTO> obsDtos = obsList.stream()
                                        .map(observationMapper::toObservationDTO)
                                        .toList();
                                ib.setObservations(obsDtos);

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
