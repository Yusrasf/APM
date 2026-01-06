package com.example.vaxregistry.web.dto;

import java.util.List;

/**
 * If {@code practitionerId} is non-null, login can proceed.
 * If {@code candidates} is non-null and size > 1, the client must let the user select.
 */
public record PractitionerLoginResponse(String practitionerId, List<ResourceCandidate> candidates) {}
