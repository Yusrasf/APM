package org.apm.backend.controller;

import org.apm.backend.dto.consent.ConsentDto;
import org.apm.backend.dto.consent.UpdateConsentRequestDto;
import org.apm.backend.service.ConsentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/{patientId}/consent")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping
    public ConsentDto getConsent(@PathVariable String patientId) {
        return consentService.getConsentForPatient(patientId);
    }

    @PutMapping
    public ConsentDto updateConsent(
            @PathVariable String patientId,
            @RequestBody UpdateConsentRequestDto request) {

        return consentService.updateConsent(patientId, request);
    }

    @DeleteMapping
    public void revokeConsent(@PathVariable String patientId) {
        consentService.revokeConsent(patientId);
    }
}
