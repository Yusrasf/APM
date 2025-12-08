import axios from "axios";
import { getAuth } from "./authApi";

const API_BASE_URL = "http://localhost:8082";

const api = axios.create({
    baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
    const auth = getAuth();
    if (auth?.basicToken) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Basic ${auth.basicToken}`;
    }
    return config;
});

export const practitionerApi = {
    getMyPatients() {
        return api.get("/api/practitioner/patients");
    },
    getPatientOverview(patientId) {
        return api.get(`/api/practitioner/patients/${patientId}/overview`);
    },
    getImmunizations(patientId) {
        return api.get(`/api/practitioner/patients/${patientId}/immunizations`);
    },
    getRecommendations(patientId) {
        return api.get(`/api/practitioner/patients/${patientId}/recommendations`);
    },
    getAppointments(patientId) {
        return api.get(`/api/practitioner/patients/${patientId}/appointments`);
    },
    createImmunization(patientId, payload) {
        return api.post(
            `/api/practitioner/patients/${patientId}/immunizations`,
            payload
        );
    },
    createRecommendation(patientId, payload) {
        return api.post(
            `/api/practitioner/patients/${patientId}/recommendations`,
            payload
        );
    },
    createAppointment(payload) {
        return api.post(`/api/practitioner/appointments`, payload);
    },
};
