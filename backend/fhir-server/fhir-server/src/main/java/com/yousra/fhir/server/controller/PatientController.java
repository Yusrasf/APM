/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.yousra.fhir.server.controller;

/**
 *
 * @author yousra
 */

import com.yousra.fhir.server.model.PatientDto;
import com.yousra.fhir.server.service.FhirPatientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private FhirPatientService patientService;

    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("->>>>>> fetching all patients - page= {}, size= {}", page, size);
        List<PatientDto> patients = patientService.getAllPatients(page, size);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchPatients(@RequestParam String name) {
        log.info("->>>>>> searching patients with name= {}", name);
        List<PatientDto> patients = patientService.searchPatients(name);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable String id) {
        log.info("->>>>>> fetching patient with id= {}", id);
        PatientDto patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@RequestBody PatientDto patientDto) {
        log.info("->>>>>> creating new patient");
        PatientDto createdPatient = patientService.createPatient(patientDto);
        return ResponseEntity.ok(createdPatient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable String id, 
            @RequestBody PatientDto patientDto) {
        log.info("->>>>>> updating patient with id= {}", id);
        PatientDto updatedPatient = patientService.updatePatient(id, patientDto);
        return ResponseEntity.ok(updatedPatient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        log.info("->>>>>> deleting patient with id= {}", id);
        patientService.deletePatient(id);
        return ResponseEntity.ok().build();
    }
}