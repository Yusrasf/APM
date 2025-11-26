package org.apm.backend.controller;

import org.apm.backend.dto.practitioner.EncounterDTO;
import org.apm.backend.dto.practitioner.ImmunizationDTO;
import org.apm.backend.dto.practitioner.PatientClinicalOverviewDTO;
import org.apm.backend.dto.practitioner.PatientDetailsDTO;
import org.apm.backend.service.PatientOverviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientOverviewService patientOverviewService;

    public PatientController(PatientOverviewService patientOverviewService) {
        this.patientOverviewService = patientOverviewService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientDetailsDTO>> searchByIdentifier(
            @RequestParam("identifier") String identifier) {

        List<PatientDetailsDTO> patients =
                patientOverviewService.searchPatientsByIdentifier(identifier);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{patientId}/encounters")
    public ResponseEntity<List<EncounterDTO>> getEncountersForPatient(
            @PathVariable String patientId) {

        List<EncounterDTO> encounters =
                patientOverviewService.getEncountersForPatient(patientId);
        return ResponseEntity.ok(encounters);
    }

    @GetMapping("/{patientId}/encounters/{encounterId}/immunizations")
    public ResponseEntity<List<ImmunizationDTO>> getImmunizationsForEncounter(
            @PathVariable String patientId,
            @PathVariable String encounterId) {

        List<ImmunizationDTO> imms =
                patientOverviewService.getImmunizationsForEncounter(encounterId);
        return ResponseEntity.ok(imms);
    }

    @GetMapping("/{patientId}/clinical-overview")
    public ResponseEntity<PatientClinicalOverviewDTO> getClinicalOverview(
            @PathVariable String patientId) {

        PatientClinicalOverviewDTO overview =
                patientOverviewService.getClinicalOverview(patientId);
        return ResponseEntity.ok(overview);
    }
}