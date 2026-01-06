package com.example.vaxregistry.web.dto;

import java.util.List;

/**
 * If {@code patientId} is non-null, login can proceed.
 * If {@code candidates} is non-null and size > 1, the client must let the user select.
 */
public record PatientLoginResponse(String patientId, List<ResourceCandidate> candidates) {}
