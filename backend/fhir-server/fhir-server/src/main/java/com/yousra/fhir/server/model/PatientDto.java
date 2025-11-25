/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.yousra.fhir.server.model;

/**
 *
 * @author yousra
 */

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PatientDto {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private List<Address> addresses;
    private List<Contact> contacts;

    @Data
    public static class Address {
        private String line;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    @Data
    public static class Contact {
        private String system;
        private String value;
        private String use;
    }
}
