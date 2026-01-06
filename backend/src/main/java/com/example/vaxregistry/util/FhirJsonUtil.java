package com.example.vaxregistry.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class FhirJsonUtil {

    private FhirJsonUtil() {}

    public static ObjectNode parseObject(ObjectMapper mapper, String json) {
        try {
            JsonNode n = mapper.readTree(json);
            if (!n.isObject()) {
                throw new IllegalArgumentException("Expected JSON object");
            }
            return (ObjectNode) n;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    public static void ensureResourceType(ObjectNode root, String resourceType) {
        if (!root.has("resourceType")) {
            root.put("resourceType", resourceType);
        }
    }

    /**
     * Ensures a FHIR Reference field exists as: { "reference": "..." }.
     * Example: Immunization.patient.reference = "Patient/{id}".
     */
    public static void ensureReference(ObjectNode root, String fieldName, String referenceValue) {
        JsonNode existing = root.get(fieldName);
        if (existing == null || existing.isNull()) {
            ObjectNode ref = root.putObject(fieldName);
            ref.put("reference", referenceValue);
            return;
        }
        if (existing.isObject()) {
            ObjectNode obj = (ObjectNode) existing;
            if (!obj.has("reference")) {
                obj.put("reference", referenceValue);
            }
            return;
        }
        // If it's a string or something unexpected, overwrite with a proper Reference object.
        ObjectNode ref = root.putObject(fieldName);
        ref.put("reference", referenceValue);
    }
}
