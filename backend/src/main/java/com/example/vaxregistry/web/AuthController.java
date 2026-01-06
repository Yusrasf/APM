package com.example.vaxregistry.web;

import com.example.vaxregistry.service.FhirClientService;
import com.example.vaxregistry.util.OperationOutcomeUtil;
import com.example.vaxregistry.web.dto.PatientLoginRequest;
import com.example.vaxregistry.web.dto.PatientLoginResponse;
import com.example.vaxregistry.web.dto.PractitionerLoginRequest;
import com.example.vaxregistry.web.dto.PractitionerLoginResponse;
import com.example.vaxregistry.web.dto.ResourceCandidate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final FhirClientService fhir;
    private final ObjectMapper mapper;

    public AuthController(FhirClientService fhir, ObjectMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    @PostMapping(path = "/patient/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginPatient(@RequestBody PatientLoginRequest req) {
        String identifier = safeTrim(req.getIdentifier());
        String name = safeTrim(req.getName());
        String birthDate = safeTrim(req.getBirthDate());

        if (identifier.isEmpty() && name.isEmpty()) {
            return badRequest("Provide either identifier or name.");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("_count", "25");
        if (!identifier.isEmpty()) {
            params.add("identifier", normalizeToken(identifier));
        } else {
            params.add("name", name);
        }
        if (!birthDate.isEmpty()) {
            // Patient search parameter is "birthdate".
            params.add("birthdate", birthDate);
        }

        String bundleJson = fhir.search("Patient", params).getBody();
        List<ResourceCandidate> all = extractCandidates(bundleJson, "Patient", true);

        if (all.isEmpty()) {
            return notFound("No Patient found for the provided criteria.");
        }

        // If birthDate was provided but server ignored it (some servers do), enforce client-side filtering as well.
        List<ResourceCandidate> filtered = all;
        if (!birthDate.isEmpty()) {
            filtered = all.stream()
                    .filter(c -> Objects.equals(birthDate, nullToEmpty(c.birthDate())))
                    .toList();
        }

        if (filtered.isEmpty()) {
            return notFound("No Patient found matching the provided birth date.");
        }
        if (filtered.size() == 1) {
            return ResponseEntity.ok(new PatientLoginResponse(filtered.get(0).id(), null));
        }
        return ResponseEntity.ok(new PatientLoginResponse(null, filtered));
    }

    @PostMapping(path = "/doctor/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginDoctor(@RequestBody PractitionerLoginRequest req) {
        String identifier = safeTrim(req.getIdentifier());
        String name = safeTrim(req.getName());

        if (identifier.isEmpty() && name.isEmpty()) {
            return badRequest("Provide either identifier or name.");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("_count", "25");
        if (!identifier.isEmpty()) {
            params.add("identifier", normalizeToken(identifier));
        } else {
            params.add("name", name);
        }

        String bundleJson = fhir.search("Practitioner", params).getBody();
        List<ResourceCandidate> candidates = extractCandidates(bundleJson, "Practitioner", false);

        if (candidates.isEmpty()) {
            return notFound("No Practitioner found for the provided criteria.");
        }
        if (candidates.size() == 1) {
            return ResponseEntity.ok(new PractitionerLoginResponse(candidates.get(0).id(), null));
        }
        return ResponseEntity.ok(new PractitionerLoginResponse(null, candidates));
    }

    private ResponseEntity<String> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(OperationOutcomeUtil.fhirJson())
                .body(OperationOutcomeUtil.error(mapper, msg));
    }

    private ResponseEntity<String> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(OperationOutcomeUtil.fhirJson())
                .body(OperationOutcomeUtil.error(mapper, msg));
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * Accept either "system|value" or just "value".
     */
    private static String normalizeToken(String input) {
        String v = input.trim();
        // HAPI and many servers expect token search as "system|value".
        // If there are multiple '|' (rare), keep as-is.
        return v;
    }

    private List<ResourceCandidate> extractCandidates(String bundleJson, String expectedType, boolean includeBirthDate) {
        try {
            JsonNode root = mapper.readTree(bundleJson == null ? "{}" : bundleJson);
            JsonNode entries = root.path("entry");
            if (!entries.isArray()) {
                return List.of();
            }
            List<ResourceCandidate> out = new ArrayList<>();
            for (JsonNode entry : entries) {
                JsonNode res = entry.path("resource");
                if (!expectedType.equals(res.path("resourceType").asText())) continue;

                String id = res.path("id").asText("");
                if (id.isEmpty()) {
                    String fullUrl = entry.path("fullUrl").asText("");
                    // fullUrl sometimes contains ".../Patient/{id}".
                    int slash = fullUrl.lastIndexOf('/');
                    if (slash >= 0 && slash + 1 < fullUrl.length()) {
                        id = fullUrl.substring(slash + 1);
                    }
                }
                if (id.isEmpty()) continue;

                String display = formatHumanName(res.path("name"));
                if (display.isEmpty()) display = expectedType + "/" + id;

                String birthDate = null;
                if (includeBirthDate) {
                    birthDate = res.path("birthDate").isTextual() ? res.path("birthDate").asText() : null;
                }

                String identifier = formatIdentifier(res.path("identifier"));
                out.add(new ResourceCandidate(id, display, birthDate, identifier));
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse FHIR Bundle: " + e.getMessage(), e);
        }
    }

    private static String formatHumanName(JsonNode nameNode) {
        if (!nameNode.isArray() || nameNode.isEmpty()) return "";
        JsonNode n = nameNode.get(0);
        if (n == null || !n.isObject()) return "";
        String text = n.path("text").asText("").trim();
        if (!text.isEmpty()) return text;

        String family = n.path("family").asText("").trim();
        String given = "";
        JsonNode givenArr = n.path("given");
        if (givenArr.isArray() && !givenArr.isEmpty()) {
            given = givenArr.get(0).asText("").trim();
        }
        String combined = (given + " " + family).trim();
        return combined;
    }

    private static String formatIdentifier(JsonNode identifierNode) {
        if (!identifierNode.isArray() || identifierNode.isEmpty()) return "";
        for (JsonNode id : identifierNode) {
            String value = id.path("value").asText("").trim();
            if (value.isEmpty()) continue;
            String system = id.path("system").asText("").trim();
            return system.isEmpty() ? value : system + "|" + value;
        }
        return "";
    }
}
