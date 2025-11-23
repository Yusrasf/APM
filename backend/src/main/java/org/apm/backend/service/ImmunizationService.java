package org.apm.backend.service;

import org.apm.backend.dto.immunization.CreateImmunizationRequestDto;
import org.apm.backend.dto.immunization.ImmunizationDto;
import org.apm.backend.mapper.ImmunizationMapper;
import org.hl7.fhir.r5.model.Immunization;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ImmunizationService {

    private final ImmunizationMapper immunizationMapper;

    // Simple in-memory store: id → resource
    private final Map<String, Immunization> immunizationStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1L);

    public ImmunizationService(ImmunizationMapper immunizationMapper) {
        this.immunizationMapper = immunizationMapper;
    }

    public ImmunizationDto createSelfReported(String patientId,
                                              CreateImmunizationRequestDto request) {

        Immunization immunization = immunizationMapper.toFHIR(patientId, request);

        String idPart = String.valueOf(idCounter.getAndIncrement());
        immunization.setId("Immunization/" + idPart);
        immunizationStore.put(idPart, immunization);

        return immunizationMapper.toDto(immunization);
    }

    public List<ImmunizationDto> getImmunizationsForPatient(String patientId) {
        if (patientId == null) {
            return new ArrayList<>();
        }

        return immunizationStore.values().stream()
                .filter(immunization ->
                        immunization.hasPatient()
                                && patientId.equals(
                                immunization.getPatient()
                                        .getReferenceElement()
                                        .getIdPart()))
                .map(immunizationMapper::toDto)
                .collect(Collectors.toList());
    }
}
