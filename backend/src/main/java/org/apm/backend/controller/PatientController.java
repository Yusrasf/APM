package org.apm.backend.controller;


import org.apm.backend.dto.patient.CreatePatientRequestDto;
import org.apm.backend.dto.patient.PatientSummaryDto;
import org.apm.backend.service.PatientService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/patient")
public class PatientController {


    private final PatientService patientService;


    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    @PostMapping("/create")
    public PatientSummaryDto createPatient(@RequestBody CreatePatientRequestDto dto) {
        return patientService.createPatient(dto);
    }


    @GetMapping("/{id}")
    public PatientSummaryDto getPatient(@PathVariable String id) {
        return patientService.getPatientById(id);
    }


    @GetMapping("/{id}/summary")
    public PatientSummaryDto getSummary(@PathVariable String id) {
        return patientService.getPatientSummary(id);
    }


    @GetMapping("/search")
    public PatientSummaryDto searchByIdentifier(@RequestParam String identifier) {
        return patientService.searchByIdentifier(identifier);
    }
}