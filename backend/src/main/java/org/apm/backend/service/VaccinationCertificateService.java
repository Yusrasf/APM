package org.apm.backend.service;

import org.apm.backend.dto.immunization.ImmunizationDto;
import org.apm.backend.dto.immunization.VaccinationCertificateDto;
import org.apm.backend.dto.patient.PatientSummaryDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class VaccinationCertificateService {

    private final PatientService patientService;
    private final ImmunizationService immunizationService;

    public VaccinationCertificateService(PatientService patientService,
                                         ImmunizationService immunizationService) {
        this.patientService = patientService;
        this.immunizationService = immunizationService;
    }

    public VaccinationCertificateDto generateCertificate(String patientId) {
        PatientSummaryDto patient = patientService.getPatientById(patientId);
        List<ImmunizationDto> immunizations =
                immunizationService.getImmunizationsForPatient(patientId);

        VaccinationCertificateDto dto = new VaccinationCertificateDto();
        dto.setPatientId(patientId);
        dto.setPatientName(patient != null ? patient.getFullName() : null);
        dto.setImmunizations(immunizations);

        // Very simple QR payload example
        dto.setQrContent("VCERT|" + patientId + "|" + OffsetDateTime.now());

        dto.setGeneratedAt(OffsetDateTime.now().toString());

        // pdfUrl can later point to a real download endpoint
        dto.setPdfUrl(null);

        return dto;
    }
}
