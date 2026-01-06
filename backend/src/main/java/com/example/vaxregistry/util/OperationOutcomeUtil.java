package com.example.vaxregistry.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;

public final class OperationOutcomeUtil {

    private OperationOutcomeUtil() {}

    public static String error(ObjectMapper mapper, String diagnostics) {
        ObjectNode root = mapper.createObjectNode();
        root.put("resourceType", "OperationOutcome");
        ArrayNode issue = root.putArray("issue");
        ObjectNode i = issue.addObject();
        i.put("severity", "error");
        i.put("code", "exception");
        i.put("diagnostics", diagnostics);
        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            // Minimal safe fallback (avoid secondary failures due to serialization)
            String d = escapeJson(diagnostics);
            return "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"" + d + "\"}]}";
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public static MediaType fhirJson() {
        return MediaType.parseMediaType("application/fhir+json");
    }
}
