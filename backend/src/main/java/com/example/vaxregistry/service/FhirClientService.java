package com.example.vaxregistry.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class FhirClientService {

    private static final MediaType FHIR_JSON = MediaType.parseMediaType("application/fhir+json");
    private static final MediaType JSON_PATCH = MediaType.parseMediaType("application/json-patch+json");

    private final RestClient rest;

    public FhirClientService(RestClient fhirRestClient) {
        this.rest = fhirRestClient;
    }

    public ResponseEntity<String> read(String resourceType, String id) {
        return rest.get()
                .uri(b -> b.pathSegment(resourceType, id).build())
                .accept(FHIR_JSON)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> search(String resourceType, MultiValueMap<String, String> params) {
        return rest.get()
                .uri(b -> b.pathSegment(resourceType).queryParams(params).build())
                .accept(FHIR_JSON)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> create(String resourceType, String fhirJson) {
        return rest.post()
                .uri(b -> b.pathSegment(resourceType).build())
                .contentType(FHIR_JSON)
                .accept(FHIR_JSON)
                .body(fhirJson)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> update(String resourceType, String id, String fhirJson) {
        return rest.put()
                .uri(b -> b.pathSegment(resourceType, id).build())
                .contentType(FHIR_JSON)
                .accept(FHIR_JSON)
                .body(fhirJson)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> patch(String resourceType, String id, String jsonPatch) {
        return rest.patch()
                .uri(b -> b.pathSegment(resourceType, id).build())
                .contentType(JSON_PATCH)
                .accept(FHIR_JSON)
                .body(jsonPatch)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> delete(String resourceType, String id) {
        return rest.delete()
                .uri(b -> b.pathSegment(resourceType, id).build())
                .accept(FHIR_JSON)
                .retrieve()
                .toEntity(String.class);
    }

    public static HttpHeaders fhirHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setAccept(java.util.List.of(FHIR_JSON));
        h.setContentType(FHIR_JSON);
        return h;
    }
}
