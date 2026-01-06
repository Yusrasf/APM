package com.example.vaxregistry.web;

import com.example.vaxregistry.model.ImmunizationCreateRequest;
import com.example.vaxregistry.model.PatientCreateRequest;
import com.example.vaxregistry.service.FhirClientService;
import com.example.vaxregistry.service.ImmunizationBuilder;
import com.example.vaxregistry.service.PatientBuilder;
import com.example.vaxregistry.service.PdfService;
import com.example.vaxregistry.util.OperationOutcomeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/physician")
public class PhysicianApiController {

    private final FhirClientService fhir;
    private final ImmunizationBuilder immunizationBuilder;
    private final PatientBuilder patientBuilder;
    private final PdfService pdfService;
    private final ObjectMapper mapper;

    public PhysicianApiController(FhirClientService fhir, ImmunizationBuilder immunizationBuilder, PatientBuilder patientBuilder, PdfService pdfService, ObjectMapper mapper) {
        this.fhir = fhir;
        this.immunizationBuilder = immunizationBuilder;
        this.patientBuilder = patientBuilder;
        this.pdfService = pdfService;
        this.mapper = mapper;
    }

    @PostMapping(value = "/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPatient(@RequestBody PatientCreateRequest req) {
        String given = req.getGivenName() == null ? "" : req.getGivenName().trim();
        String family = req.getFamilyName() == null ? "" : req.getFamilyName().trim();
        String idVal = req.getIdentifierValue() == null ? "" : req.getIdentifierValue().trim();
        if (given.isEmpty() && family.isEmpty() && idVal.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(OperationOutcomeUtil.fhirJson())
                    .body(OperationOutcomeUtil.error(mapper, "Provide at least a name or an identifier value."));
        }

        String patientJson = patientBuilder.build(req);
        ResponseEntity<String> createRes = fhir.create("Patient", patientJson);

        String newId = extractIdFromCreateResponse(createRes);
        if (newId == null || newId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(OperationOutcomeUtil.fhirJson())
                    .body(OperationOutcomeUtil.error(mapper, "FHIR server did not return a Patient id."));
        }

        return ResponseEntity.ok(java.util.Map.of("patientId", newId));
    }

    private String extractIdFromCreateResponse(ResponseEntity<String> res) {
        // 1) Try body (some servers return the created resource)
        try {
            String body = res.getBody();
            if (body != null && !body.isBlank()) {
                JsonNode root = mapper.readTree(body);
                if ("Patient".equals(root.path("resourceType").asText(""))) {
                    String id = root.path("id").asText("");
                    if (!id.isBlank()) return id;
                }
            }
        } catch (Exception ignored) {
        }

        // 2) Location header (most common)
        String location = res.getHeaders().getFirst("Location");
        if (location == null || location.isBlank()) {
            location = res.getHeaders().getFirst("Content-Location");
        }
        if (location == null || location.isBlank()) return null;

        // Strip history
        int hist = location.indexOf("/_history/");
        if (hist >= 0) location = location.substring(0, hist);

        // Get last path segment
        int slash = location.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < location.length()) {
            return location.substring(slash + 1);
        }
        return null;
    }

    /**
     * Best-effort list of patients for a practitioner.
     * Not all public test servers support the general-practitioner search parameter.
     */
    @GetMapping(value = "/{practitionerId}/patients", produces = "application/fhir+json")
    public ResponseEntity<String> patientsForPractitioner(@PathVariable String practitionerId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // FHIR search param: Patient?general-practitioner=Practitioner/{id}
        params.add("general-practitioner", "Practitioner/" + practitionerId);
        return fhir.search("Patient", params);
    }

    @GetMapping(value = "/patients/search", produces = "application/fhir+json")
    public ResponseEntity<String> searchPatientsByIdentifier(@RequestParam String identifier) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("identifier", identifier);
        return fhir.search("Patient", params);
    }

    /**
     * UI-friendly patient search with simple heuristics.
     *
     * - contains '|' => identifier token
     * - single token => try _id then identifier
     * - otherwise => name
     */
    @GetMapping(value = "/patients/search", params = "term", produces = "application/fhir+json")
    public ResponseEntity<String> searchPatients(@RequestParam String term) {
        String q = term == null ? "" : term.trim();
        if (q.isEmpty()) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("_count", "25");
            return fhir.search("Patient", params);
        }

        boolean looksLikeToken = q.contains("|");
        boolean singleToken = !q.contains(" ") && q.length() <= 64;

        if (looksLikeToken) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("identifier", q);
            params.add("_count", "25");
            return fhir.search("Patient", params);
        }

        if (singleToken) {
            // Try _id first
            MultiValueMap<String, String> idParams = new LinkedMultiValueMap<>();
            idParams.add("_id", q);
            idParams.add("_count", "25");
            ResponseEntity<String> byId = fhir.search("Patient", idParams);
            if (hasEntries(byId.getBody())) {
                return byId;
            }

            // Fallback to identifier
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("identifier", q);
            params.add("_count", "25");
            return fhir.search("Patient", params);
        }

        // Default: name search
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", q);
        params.add("_count", "25");
        return fhir.search("Patient", params);
    }

    private boolean hasEntries(String bundleJson) {
        try {
            JsonNode root = mapper.readTree(bundleJson == null ? "{}" : bundleJson);
            JsonNode entry = root.path("entry");
            return entry.isArray() && entry.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping(value = "/patients/{patientId}/immunizations", produces = "application/fhir+json")
    public ResponseEntity<String> immunizations(@PathVariable String patientId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("patient", patientId);
        return fhir.search("Immunization", params);
    }

    @PostMapping(value = "/patients/{patientId}/immunizations", produces = "application/fhir+json")
    public ResponseEntity<String> createImmunization(
            @PathVariable String patientId,
            @Valid @RequestBody ImmunizationCreateRequest req
    ) {
        String json = immunizationBuilder.buildForPatient(patientId, req, false);
        return fhir.create("Immunization", json);
    }

    @GetMapping(value = "/patients/{patientId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> report(@PathVariable String patientId, @RequestParam(required = false) String note) {
        String qr = "vax-registry://report?patient=" + patientId + "&ts=" + OffsetDateTime.now();
        byte[] pdf = pdfService.generatePhysicianReportPdf(patientId, qr, note);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + patientId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
