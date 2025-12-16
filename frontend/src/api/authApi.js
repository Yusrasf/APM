// src/api/authApi.js
import axios from "axios";

const API_BASE_URL = "http://localhost:8082";
const AUTH_KEY = "auth";

export async function login(identifier, password) {
    const response = await axios.post(`${API_BASE_URL}/api/auth/login`, {
        identifier,
        password,
    });

    // Server returns: { accessToken, practitioner }
    const { accessToken, practitioner } = response.data;

    // Your original Basic Auth
    const basicToken = btoa(`${identifier}:${password}`);

    const authData = {
        identifier,
        password,
        basicToken,
        accessToken,
        practitioner
    };

    // Save ONLY ONE THING in localStorage (your original behavior)
    localStorage.setItem(AUTH_KEY, JSON.stringify(authData));

    return authData;
}

export function getAuth() {
    const raw = localStorage.getItem(AUTH_KEY);
    if (!raw) return null;

    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
}

export function logout() {
    localStorage.removeItem(AUTH_KEY);
}
