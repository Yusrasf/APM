# FHIR-Based Vaccination Registry (Patient + Physician)

This repository is a complete starter codebase for the **FHIR-Based Vaccination Registry** described in your sprint documents.

- **Backend:** Spring Boot + Maven. Proxies CRUD calls to a FHIR R4 server and provides convenience endpoints for the user flow (vaccination history, self-reported immunizations, PDF/QR certificate, physician report).
- **Frontend:** Vue 3 + Vite. Simple patient and physician dashboards that call the backend.

The implementation is aligned with:
- Using FHIR R4 resources (Patient, Practitioner, Immunization, Organization, etc.) and their relationships.
- Exposing a minimal REST interface for CRUD (Patient/Practitioner/Organization/Immunization/ImmunizationRecommendation).
- Running without a local DB by default and relying on a FHIR server.

## Quick start

### 1) Backend

Requirements: Java 17+ (the project compiles to Java 17 bytecode; Spring Boot 3.5.x is compatible with newer runtimes, including Java 25)

```bash
cd backend
mvn spring-boot:run
```

Note: the Maven goal is `spring-boot:run` (with a dash). `spring:run` will fail with “No plugin found for prefix 'spring'”.

The backend starts on `http://localhost:8080`.

By default it targets the public HAPI test server:

- `FHIR_BASE_URL=https://hapi.fhir.org/baseR4`

You can override it:

```bash
FHIR_BASE_URL=http://localhost:8081/fhir mvn spring-boot:run
```

### 2) Frontend

Requirements: Node 18+

```bash
cd frontend
npm install
npm run dev
```

The UI runs on `http://localhost:5173`.

If you run a production build (`npm run build` + `npm run preview`), set `VITE_BACKEND_URL` so the UI knows where the backend is:

```bash
cp .env.example .env.local
# then edit VITE_BACKEND_URL if needed
```

## Optional: run a local HAPI FHIR server

A docker compose is provided:

```bash
docker compose up -d
```

This starts a HAPI FHIR JPA server on `http://localhost:8081/fhir`.

## API

### A) Generic CRUD proxy (FHIR JSON)

These endpoints forward requests to the configured FHIR server and return the raw FHIR response.

- `GET /Patient?name=smith`
- `GET /Patient/{id}`
- `POST /Patient`
- `PUT /Patient/{id}`
- `PATCH /Patient/{id}` (JSON Patch)
- `DELETE /Patient/{id}`

Same pattern is enabled for:

`Patient`, `Practitioner`, `Organization`, `Immunization`, `ImmunizationRecommendation`, `Consent`, `AdverseEvent`, `Encounter`, `Appointment`, `Schedule`, `Slot`, `ServiceRequest`, `PractitionerRole`, `Location`, `AllergyIntolerance`, `Condition`.

### B) Convenience endpoints (user flow)

**Patient**
- `GET /api/patient/{patientId}/immunizations`
- `POST /api/patient/{patientId}/self-reported-immunizations`
- `GET /api/patient/{patientId}/certificate` (PDF with QR)
- `GET /api/patient/{patientId}/consents`

**Physician**
- `GET /api/physician/{practitionerId}/patients` (best-effort; uses `general-practitioner` search if supported)
- `GET /api/physician/patients/search?identifier=...`
- `GET /api/physician/patients/{patientId}/immunizations`
- `POST /api/physician/patients/{patientId}/immunizations`
- `GET /api/physician/patients/{patientId}/report` (PDF report)

## REST examples

See `api/rest/` for ready-to-run `.rest` request files.

## Notes

- Authentication/OIDC is left as **optional** scaffolding (see backend `application.yml` and the comments in code). This keeps the project runnable against a public test server without Keycloak setup.
- The public HAPI FHIR test server is shared and can purge data; use the local docker option for stable testing.

