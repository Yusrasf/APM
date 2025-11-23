package org.apm.backend.service;

import org.apm.backend.dto.consent.ConsentDto;
import org.apm.backend.dto.consent.UpdateConsentRequestDto;
import org.apm.backend.mapper.ConsentMapper;
import org.hl7.fhir.r5.model.Consent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ConsentService {

    private final ConsentMapper consentMapper;

    // store one Consent per patient
    private final Map<String, Consent> consentByPatientId = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1L);

    public ConsentService(ConsentMapper consentMapper) {
        this.consentMapper = consentMapper;
    }

    public ConsentDto getConsentForPatient(String patientId) {
        Consent consent = consentByPatientId.get(patientId);
        if (consent == null) {
            return null;
        }
        return consentMapper.toDto(consent);
    }

    public ConsentDto updateConsent(String patientId, UpdateConsentRequestDto request) {
        Consent consent = consentMapper.toFHIR(patientId, request);

        // assign id (create or replace)
        String idPart = consentByPatientId.containsKey(patientId)
                ? consentByPatientId.get(patientId).getIdElement().getIdPart()
                : String.valueOf(idCounter.getAndIncrement());

        consent.setId("Consent/" + idPart);
        consentByPatientId.put(patientId, consent);

        return consentMapper.toDto(consent);
    }

    public void revokeConsent(String patientId) {
        Consent consent = consentByPatientId.get(patientId);
        if (consent != null) {
            consent.setStatus(Consent.ConsentState.INACTIVE);
        }
    }
}
