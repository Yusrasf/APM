// src/api/authApi.js
import axios from "axios";

const API_BASE_URL = "http://localhost:8082";
const AUTH_KEY = "auth";

export async function login(identifier, password) {
    const response = await axios.post(`${API_BASE_URL}/api/auth/login`, {
        identifier,
        password,
    });

    const { accessToken, practitioner } = response.data;

    const basicToken = btoa(`${identifier}:${password}`);

    const authData = {
        identifier,
        password,
        basicToken,
        accessToken,
        practitioner,
    };

    localStorage.setItem(AUTH_KEY, JSON.stringify(authData));
    localStorage.setItem("practitioner", JSON.stringify(practitioner));
    localStorage.setItem("basicToken", basicToken);

    return authData;
}

export function getAuth() {
    const raw = localStorage.getItem(AUTH_KEY);
    if (!raw) return null;
    return JSON.parse(raw);
}

export function logout() {
    localStorage.removeItem(AUTH_KEY);
    localStorage.removeItem("practitioner");
    localStorage.removeItem("basicToken");
}

export const registerPractitioner = (identifier, password) => {
    return axios.post(`${API_BASE_URL}/api/auth/register`, {
        identifier,
        password
    });
};

