package com.example.vaxregistry.service;

import com.example.vaxregistry.model.ImmunizationCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationBuilder {

    private final ObjectMapper mapper;

    public ImmunizationBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String buildForPatient(String patientId, ImmunizationCreateRequest req, boolean selfReported) {
        ObjectNode root = mapper.createObjectNode();
        root.put("resourceType", "Immunization");
        root.put("status", "completed");

        // patient reference
        ObjectNode patient = root.putObject("patient");
        patient.put("reference", "Patient/" + patientId);

        // vaccine code
        ObjectNode vaccineCode = root.putObject("vaccineCode");
        ArrayNode coding = vaccineCode.putArray("coding");
        ObjectNode coding0 = coding.addObject();
        coding0.put("system", req.getVaccineSystem());
        coding0.put("code", req.getVaccineCode());
        if (req.getVaccineDisplay() != null && !req.getVaccineDisplay().isBlank()) {
            coding0.put("display", req.getVaccineDisplay());
        }

        // occurrenceDateTime
        root.put("occurrenceDateTime", req.getOccurrenceDateTime());

        if (req.getLotNumber() != null && !req.getLotNumber().isBlank()) {
            root.put("lotNumber", req.getLotNumber());
        }

        if (req.getNote() != null && !req.getNote().isBlank()) {
            ArrayNode notes = root.putArray("note");
            ObjectNode n = notes.addObject();
            n.put("text", req.getNote());
        }

        if (selfReported) {
            // This is not a custom extension; we just put a reasonable note.
            ArrayNode notes = root.withArray("note");
            ObjectNode n = notes.addObject();
            n.put("text", "Self-reported vaccination (patient-entered).");
        }

        // performer
        if (req.getPerformerPractitionerId() != null && !req.getPerformerPractitionerId().isBlank()) {
            ArrayNode performers = root.putArray("performer");
            ObjectNode p = performers.addObject();
            ObjectNode actor = p.putObject("actor");
            actor.put("reference", "Practitioner/" + req.getPerformerPractitionerId());
        }

        // organization (manufacturer is a Reference to Organization)
        if (req.getOrganizationId() != null && !req.getOrganizationId().isBlank()) {
            ObjectNode manufacturer = root.putObject("manufacturer");
            manufacturer.put("reference", "Organization/" + req.getOrganizationId());
        }

        // location
        if (req.getLocationId() != null && !req.getLocationId().isBlank()) {
            ObjectNode location = root.putObject("location");
            location.put("reference", "Location/" + req.getLocationId());
        }

        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build Immunization JSON", e);
        }
    }
}
