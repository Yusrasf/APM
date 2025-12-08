import axios from "axios";

const API_BASE_URL = "http://localhost:8080";

function getAuthHeaders() {
    const stored = localStorage.getItem("practitionerAuth");
    if (!stored) return {};
    try {
        const { username, password } = JSON.parse(stored);
        if (!username || !password) return {};
        const token = btoa(`${username}:${password}`);
        return { Authorization: `Basic ${token}` };
    } catch {
        return {};
    }
}

function get(url) {
    return axios.get(`${API_BASE_URL}${url}`, {
        headers: getAuthHeaders(),
    });
}

function post(url, data) {
    return axios.post(`${API_BASE_URL}${url}`, data, {
        headers: {
            "Content-Type": "application/json",
            ...getAuthHeaders(),
        },
    });
}

export const practitionerApi = {
    getPatients() {
        return get("/api/practitioner/patients");
    },
    getPatientOverview(patientId) {
        return get(`/api/practitioner/patients/${patientId}/overview`);
    },
    getImmunizations(patientId) {
        return get(`/api/practitioner/patients/${patientId}/immunizations`);
    },
    getRecommendations(patientId) {
        return get(`/api/practitioner/patients/${patientId}/recommendations`);
    },
    getAppointments(patientId) {
        return get(`/api/practitioner/patients/${patientId}/appointments`);
    },
    createImmunization(patientId, payload) {
        return post(`/api/practitioner/patients/${patientId}/immunizations`, payload);
    },
    createRecommendation(patientId, payload) {
        return post(
            `/api/practitioner/patients/${patientId}/recommendations`,
            payload
        );
    },
    createAppointment(payload) {
        return post(`/api/practitioner/appointments`, payload);
    },
};
