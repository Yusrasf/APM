// src/api/practitionerApi.js
import axios from "axios";

const API_BASE_URL = "http://localhost:8082";

const api = axios.create({
    baseURL: API_BASE_URL,
});

// Attach Basic auth from localStorage
api.interceptors.request.use((config) => {
    const basicToken = localStorage.getItem("basicToken");
    if (basicToken) {
        config.headers.Authorization = `Basic ${basicToken}`;
    }
    return config;
});

// ---- PATIENTS -------------------------------------------------------------


export const fetchMyPatients = async () => {
    const res = await api.get("/api/practitioner/patients");
    return res.data;
};

export const fetchPatientOverview = async (patientId) => {
    const res = await api.get(`/api/practitioner/patients/${patientId}/overview`);
    return res.data; // PatientClinicalOverviewDTO
};

// ---- IMMUNIZATIONS --------------------------------------------------------

export const fetchImmunizations = async (patientId) => {
    const res = await api.get(
        `/api/practitioner/patients/${patientId}/immunizations`
    );
    return res.data; // List<ImmunizationDTO>
};

export const createImmunization = async (patientId, payload) => {
    // payload: { vaccineCode, vaccineDisplay, date, lotNumber }
    const res = await api.post(
        `/api/practitioner/patients/${patientId}/immunizations`,
        payload
    );
    return res.data;
};

// ---- RECOMMENDATIONS ------------------------------------------------------

export const fetchRecommendations = async (patientId) => {
    const res = await api.get(
        `/api/practitioner/patients/${patientId}/recommendations`
    );
    return res.data; // List<ImmunizationRecommendationDTO>
};

export const createRecommendation = async (patientId, payload) => {
    // payload: { vaccineCode, vaccineDisplay, dueDate, series, doseNumber, notes }
    const res = await api.post(
        `/api/practitioner/patients/${patientId}/recommendations`,
        payload
    );
    return res.data;
};

// ---- APPOINTMENTS ---------------------------------------------------------

export const fetchAppointments = async (patientId) => {
    const res = await api.get(
        `/api/practitioner/patients/${patientId}/appointments`
    );
    return res.data; // List<AppointmentDTO>
};

export const createAppointment = async (payload) => {
    // payload: { patientId, start, end, reason, location }
    const res = await api.post("/api/practitioner/appointments", payload);
    return res.data;
};

export const updateAppointmentStatus = async (appointmentId, status) => {
    const res = await api.put(
        `/api/practitioner/appointments/${appointmentId}/status`,
        null,
        { params: { status } }
    );
    return res.data;
};
