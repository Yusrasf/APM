package org.apm.backend.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r5.model.Patient;
import org.apm.backend.dto.patient.CreatePatientRequestDto;
import org.apm.backend.dto.patient.PatientSummaryDto;
import org.apm.backend.fhir.PatientResourceProvider;
import org.apm.backend.mapper.PatientMapper;
import org.springframework.stereotype.Service;

@Service
public class PatientService {
    private final PatientMapper patientMapper;
    private final PatientResourceProvider patientResourceProvider;
    public PatientService(PatientMapper patientMapper, PatientResourceProvider patientResourceProvider) {
        this.patientMapper = patientMapper;
        this.patientResourceProvider = patientResourceProvider;
    }
    public PatientSummaryDto createPatient(CreatePatientRequestDto dto) {
        Patient patient = patientMapper.toFHIR(dto);
        MethodOutcome outcome = patientResourceProvider.create(patient);
        Patient saved = (Patient) outcome.getResource();

        return patientMapper.toPatientSummaryDto(saved);
    }
    public PatientSummaryDto getPatientById(String id) {
        Patient patient = patientResourceProvider.read(new org.hl7.fhir.r5.model.IdType(id));
        return patientMapper.toPatientSummaryDto(patient);
    }

    public PatientSummaryDto searchByIdentifier(String identifier) {
        return patientResourceProvider.searchByIdentifier(new ca.uhn.fhir.rest.param.TokenParam(identifier))
                .stream()
                .findFirst()
                .map(patientMapper::toPatientSummaryDto)
                .orElse(null);
    }
    public PatientSummaryDto getPatientSummary(String id) {
        return getPatientById(id);
    }
}
