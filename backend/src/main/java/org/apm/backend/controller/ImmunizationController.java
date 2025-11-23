package org.apm.backend.controller;

import org.apm.backend.dto.immunization.CreateImmunizationRequestDto;
import org.apm.backend.dto.immunization.ImmunizationDto;
import org.apm.backend.service.ImmunizationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/{patientId}/immunizations")
public class ImmunizationController {

    private final ImmunizationService immunizationService;

    public ImmunizationController(ImmunizationService immunizationService) {
        this.immunizationService = immunizationService;
    }

    /**
     * Patient vaccination history.
     */
    @GetMapping
    public List<ImmunizationDto> getImmunizations(@PathVariable String patientId) {
        return immunizationService.getImmunizationsForPatient(patientId);
    }

    /**
     * Add a self-reported vaccination.
     */
    @PostMapping
    public ImmunizationDto addSelfReported(
            @PathVariable String patientId,
            @RequestBody CreateImmunizationRequestDto request) {

        return immunizationService.createSelfReported(patientId, request);
    }
}
