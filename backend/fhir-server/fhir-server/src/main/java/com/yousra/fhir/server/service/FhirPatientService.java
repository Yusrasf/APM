/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.yousra.fhir.server.service;

/**
 *
 * @author yousra
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.yousra.fhir.server.model.PatientDto;
import com.yousra.fhir.server.util.FhirUtils;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FhirPatientService {

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirContext fhirContext;

    public List<PatientDto> searchPatients(String name) {
        try {
            Bundle bundle = fhirClient.search()
                    .forResource(Patient.class)
                    .where(Patient.NAME.matches().value(name))
                    .returnBundle(Bundle.class)
                    .execute();

            return bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(resource -> resource instanceof Patient)
                    .map(resource -> FhirUtils.patientToDto((Patient) resource))
                    .collect(Collectors.toList());
        } catch (FhirClientConnectionException e) {
            log.error("error while connecting to FHIR server", e);
            throw new RuntimeException("Unable to connect to FHIR server");
        }
    }

    public PatientDto getPatientById(String id) {
        try {
            Patient patient = fhirClient.read()
                    .resource(Patient.class)
                    .withId(id)
                    .execute();
            return FhirUtils.patientToDto(patient);
        } catch (Exception e) {
            log.error("error while fetching patient with id= {}", id, e);
            throw new RuntimeException("patient not found or error fetching patient");
        }
    }

public PatientDto createPatient(PatientDto patientDto) {
    try {
        Patient patient = FhirUtils.dtoToPatient(patientDto);
        
        ca.uhn.fhir.rest.api.MethodOutcome outcome = fhirClient.create()
                .resource(patient)
                .execute();
        
        Patient createdPatient = (Patient) outcome.getResource();
        
        return FhirUtils.patientToDto(createdPatient);
    } catch (Exception e) {
        log.error("error while creating new patient", e);
        throw new RuntimeException("error while creating new patient");
    }
}

public PatientDto updatePatient(String id, PatientDto patientDto) {
    try {
        Patient patient = FhirUtils.dtoToPatient(patientDto);
        patient.setId(id);
        
        ca.uhn.fhir.rest.api.MethodOutcome outcome = fhirClient.update()
                .resource(patient)
                .execute();
        
        Patient updatedPatient = (Patient) outcome.getResource();
        
        return FhirUtils.patientToDto(updatedPatient);
    } catch (Exception e) {
        log.error("error while updating patient with id= {}", id, e);
        throw new RuntimeException("error while updating patient");
    }
}
    public void deletePatient(String id) {
        try {
            fhirClient.delete()
                    .resourceById("Patient", id)
                    .execute();
        } catch (Exception e) {
            log.error("error while deleting patient with id= {}", id, e);
            throw new RuntimeException("error while deleting patient");
        }
    }

    public List<PatientDto> getAllPatients(int page, int size) {
        try {
            Bundle bundle = fhirClient.search()
                    .forResource(Patient.class)
                    .count(size)
                    .offset(page * size)
                    .returnBundle(Bundle.class)
                    .execute();

            return bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(resource -> resource instanceof Patient)
                    .map(resource -> FhirUtils.patientToDto((Patient) resource))
                    .collect(Collectors.toList());
        } catch (FhirClientConnectionException e) {
            log.error("error while connecting to FHIR server", e);
            throw new RuntimeException("error while connecting to FHIR server");
        }
    }
}