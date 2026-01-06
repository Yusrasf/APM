package com.example.vaxregistry.web;

import com.example.vaxregistry.service.IceDecisionSupportService;
import com.example.vaxregistry.util.OperationOutcomeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/cds")
public class IceCdsController {

    private final IceDecisionSupportService ice;
    private final ObjectMapper mapper;

    public IceCdsController(IceDecisionSupportService ice, ObjectMapper mapper) {
        this.ice = ice;
        this.mapper = mapper;
    }

    @GetMapping(value = "/ice/forecast/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> forecast(@PathVariable String patientId,
                                     @RequestParam(required = false) String specifiedTime) {
        try {
            OffsetDateTime t;
            if (specifiedTime == null || specifiedTime.isBlank()) {
                t = OffsetDateTime.now();
            } else {
                t = OffsetDateTime.parse(specifiedTime);
            }
            return ResponseEntity.ok(ice.forecastForPatient(patientId, t));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(OperationOutcomeUtil.fhirJson())
                    .body(OperationOutcomeUtil.error(mapper, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(OperationOutcomeUtil.fhirJson())
                    .body(OperationOutcomeUtil.error(mapper, "External decision support failed: " + e.getMessage()));
        }
    }
}
