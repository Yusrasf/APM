/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.yousra.fhir.server.util;

/**
 *
 * @author yousra
 */

import com.yousra.fhir.server.model.*;
import org.hl7.fhir.r4.model.*;
import java.util.List;
import java.util.stream.Collectors;

public class FhirUtils {

    public static Patient dtoToPatient(PatientDto dto) {
        Patient patient = new Patient();
        
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            patient.setId(dto.getId());
        }
        
        HumanName name = patient.addName();
        name.addGiven(dto.getFirstName());
        name.setFamily(dto.getLastName());
        
        if (dto.getBirthDate() != null) {
            patient.setBirthDate(java.sql.Date.valueOf(dto.getBirthDate()));
        }
        
        if (dto.getGender() != null) {
            patient.setGender(Enumerations.AdministrativeGender.fromCode(dto.getGender().toLowerCase()));
        }
        
        if (dto.getAddresses() != null) {
            dto.getAddresses().forEach(addressDto -> {
                Address address = patient.addAddress();
                address.addLine(addressDto.getLine());
                address.setCity(addressDto.getCity());
                address.setState(addressDto.getState());
                address.setPostalCode(addressDto.getPostalCode());
                address.setCountry(addressDto.getCountry());
            });
        }
        
        if (dto.getContacts() != null) {
            dto.getContacts().forEach(contactDto -> {
                ContactPoint contactPoint = patient.addTelecom();
                contactPoint.setSystem(ContactPoint.ContactPointSystem.fromCode(contactDto.getSystem()));
                contactPoint.setValue(contactDto.getValue());
                if (contactDto.getUse() != null) {
                    contactPoint.setUse(ContactPoint.ContactPointUse.fromCode(contactDto.getUse()));
                }
            });
        }
        
        return patient;
    }

    public static PatientDto patientToDto(Patient patient) {
        PatientDto dto = new PatientDto();
        
        dto.setId(patient.getIdElement().getIdPart());
        
        if (!patient.getName().isEmpty()) {
            HumanName name = patient.getNameFirstRep();
            if (!name.getGiven().isEmpty()) {
                dto.setFirstName(name.getGivenAsSingleString());
            }
            dto.setLastName(name.getFamily());
        }
        
        if (patient.getBirthDate() != null) {
            dto.setBirthDate(patient.getBirthDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        }
        
        if (patient.getGender() != null) {
            dto.setGender(patient.getGender().toCode());
        }
        
        List<PatientDto.Address> addresses = patient.getAddress().stream()
                .map(address -> {
                    PatientDto.Address addressDto = new PatientDto.Address();
                    if (!address.getLine().isEmpty()) {
                        addressDto.setLine(address.getLine().get(0).getValue());
                    }
                    addressDto.setCity(address.getCity());
                    addressDto.setState(address.getState());
                    addressDto.setPostalCode(address.getPostalCode());
                    addressDto.setCountry(address.getCountry());
                    return addressDto;
                })
                .collect(Collectors.toList());
        dto.setAddresses(addresses);
        
        List<PatientDto.Contact> contacts = patient.getTelecom().stream()
                .map(contact -> {
                    PatientDto.Contact contactDto = new PatientDto.Contact();
                    contactDto.setSystem(contact.getSystem().toCode());
                    contactDto.setValue(contact.getValue());
                    if (contact.getUse() != null) {
                        contactDto.setUse(contact.getUse().toCode());
                    }
                    return contactDto;
                })
                .collect(Collectors.toList());
        dto.setContacts(contacts);
        
        return dto;
    }
}