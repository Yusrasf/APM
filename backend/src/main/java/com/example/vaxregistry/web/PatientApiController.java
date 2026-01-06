package com.example.vaxregistry.web;

import com.example.vaxregistry.model.ImmunizationCreateRequest;
import com.example.vaxregistry.service.FhirClientService;
import com.example.vaxregistry.service.ImmunizationBuilder;
import com.example.vaxregistry.service.PdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/patient")
public class PatientApiController {

    private final FhirClientService fhir;
    private final ImmunizationBuilder immunizationBuilder;
    private final PdfService pdfService;

    public PatientApiController(FhirClientService fhir, ImmunizationBuilder immunizationBuilder, PdfService pdfService) {
        this.fhir = fhir;
        this.immunizationBuilder = immunizationBuilder;
        this.pdfService = pdfService;
    }

    @GetMapping(value = "/{patientId}/immunizations", produces = "application/fhir+json")
    public ResponseEntity<String> immunizations(@PathVariable String patientId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("patient", patientId);
        return fhir.search("Immunization", params);
    }

    @PostMapping(value = "/{patientId}/self-reported-immunizations", produces = "application/fhir+json")
    public ResponseEntity<String> createSelfReported(
            @PathVariable String patientId,
            @Valid @RequestBody ImmunizationCreateRequest req
    ) {
        String json = immunizationBuilder.buildForPatient(patientId, req, true);
        return fhir.create("Immunization", json);
    }

    @GetMapping(value = "/{patientId}/certificate", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> certificate(@PathVariable String patientId) {
        String qr = "vax-registry://certificate?patient=" + patientId + "&ts=" + OffsetDateTime.now();
        byte[] pdf = pdfService.generateCertificatePdf(patientId, qr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + patientId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping(value = "/{patientId}/consents", produces = "application/fhir+json")
    public ResponseEntity<String> consents(@PathVariable String patientId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("patient", patientId);
        return fhir.search("Consent", params);
    }
}
