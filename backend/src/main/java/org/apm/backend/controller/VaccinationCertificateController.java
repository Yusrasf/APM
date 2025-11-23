package org.apm.backend.controller;

import org.apm.backend.dto.immunization.VaccinationCertificateDto;
import org.apm.backend.service.VaccinationCertificateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/{patientId}/certificate")
public class VaccinationCertificateController {

    private final VaccinationCertificateService certificateService;

    public VaccinationCertificateController(VaccinationCertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public VaccinationCertificateDto getCertificate(@PathVariable String patientId) {
        return certificateService.generateCertificate(patientId);
    }
}
