package com.example.vaxregistry.service;

import com.example.vaxregistry.model.PatientCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class PatientBuilder {

    private final ObjectMapper mapper;

    public PatientBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String build(PatientCreateRequest req) {
        ObjectNode root = mapper.createObjectNode();
        root.put("resourceType", "Patient");

        // Name
        String given = safe(req.getGivenName());
        String family = safe(req.getFamilyName());
        if (!given.isEmpty() || !family.isEmpty()) {
            ArrayNode names = root.putArray("name");
            ObjectNode n = names.addObject();
            if (!family.isEmpty()) n.put("family", family);
            if (!given.isEmpty()) {
                ArrayNode givenArr = n.putArray("given");
                for (String part : given.split("\\s+")) {
                    if (!part.isBlank()) givenArr.add(part);
                }
            }
        }

        // Birth date
        String birthDate = safe(req.getBirthDate());
        if (!birthDate.isEmpty()) {
            root.put("birthDate", birthDate);
        }

        // Gender
        String gender = safe(req.getGender()).toLowerCase();
        if (gender.equals("male") || gender.equals("female") || gender.equals("other") || gender.equals("unknown")) {
            root.put("gender", gender);
        }

        // Identifier
        String idVal = safe(req.getIdentifierValue());
        if (!idVal.isEmpty()) {
            ArrayNode ids = root.putArray("identifier");
            ObjectNode id0 = ids.addObject();
            String sys = safe(req.getIdentifierSystem());
            if (!sys.isEmpty()) id0.put("system", sys);
            id0.put("value", idVal);
        }

        // Country (address.country)
        String country = safe(req.getCountry());
        if (!country.isEmpty()) {
            ArrayNode addr = root.putArray("address");
            ObjectNode a0 = addr.addObject();
            a0.put("country", country);
        }

        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build Patient JSON", e);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
