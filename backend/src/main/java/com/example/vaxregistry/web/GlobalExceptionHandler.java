package com.example.vaxregistry.web;

import com.example.vaxregistry.util.OperationOutcomeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType FHIR_JSON = MediaType.parseMediaType("application/fhir+json");

    private final ObjectMapper mapper;

    public GlobalExceptionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(FHIR_JSON)
                .body(OperationOutcomeUtil.error(mapper, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAny(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(FHIR_JSON)
                .body(OperationOutcomeUtil.error(mapper, e.getMessage()));
    }
}
