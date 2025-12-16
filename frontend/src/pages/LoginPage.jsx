import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authApi";

const containerStyle = {
    display: "flex",
    minHeight: "100vh",
};

const columnStyle = {
    flex: 1,
    padding: "3rem",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
};

const leftStyle = {
    ...columnStyle,
    background: "#f5f7fb",
};

const rightStyle = {
    ...columnStyle,
    background: "#ffffff",
    borderLeft: "1px solid #e0e0e0",
};

const cardStyle = {
    maxWidth: "420px",
    margin: "0 auto",
};

const inputStyle = {
    width: "100%",
    padding: "0.6rem 0.75rem",
    marginBottom: "0.75rem",
    borderRadius: "6px",
    border: "1px solid #ccc",
    fontSize: "0.95rem",
};

function LoginPage() {
    const navigate = useNavigate();
    const [identifier, setIdentifier] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handlePractitionerLogin = async (e) => {
        e.preventDefault();

        if (!identifier || !password) {
            setError("Please enter identifier and password.");
            return;
        }

        try {
            setError("");
            setLoading(true);

            const response = await login(identifier, password);

            //  FIX: store Basic Auth token for all backend requests
            const basic = btoa(identifier + ":" + password);
            localStorage.setItem("basicToken", basic);

            // Store practitioner login name
            localStorage.setItem("practitionerName", identifier);

            // Optional: store JWT if backend returns it
            if (response?.accessToken) {
                localStorage.setItem("accessToken", response.accessToken);
            }

            navigate("/practitioner");

        } catch (err) {
            console.error(err);
            setError(
                err.response?.data?.message ||
                "Login failed. Please check your credentials."
            );
        } finally {
            setLoading(false);
        }
    };


    return (
        <div style={containerStyle}>
            <div style={leftStyle}>
                <div style={cardStyle}>
                    <h1>Vaccination Tracking System</h1>
                    <p style={{ marginBottom: "2rem", color: "#555" }}>
                        Patients can view their immunizations history and the upcoming immunization recommendations,
                        appointments.
                    </p>
                    <h2>Pacient login</h2>
                    <p style={{ fontSize: "0.9rem", color: "#666" }}>
                        To be implemented later. Use the practitioner login on the right to
                        test the system.
                    </p>
                </div>
            </div>

            <div style={rightStyle}>
                <div style={cardStyle}>
                    <h2>Practitioner Login</h2>
                    {/*  <p style={{ marginBottom: "1.5rem", fontSize: "0.9rem", color: "#666" }}>
                   Use the credentials from <code>practitioner-credentials.json</code>.
                  </p> */}

                    <form onSubmit={handlePractitionerLogin}>
                        <label style={{ fontSize: "0.85rem", fontWeight: 600 }}>
                            Identifier
                        </label>
                        <input
                            style={inputStyle}
                            value={identifier}
                            onChange={(e) => setIdentifier(e.target.value)}
                            placeholder="practitioner ID"
                        />

                        <label style={{ fontSize: "0.85rem", fontWeight: 600 }}>
                            Password
                        </label>
                        <input
                            style={inputStyle}
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                        />

                        {error && (
                            <div
                                style={{
                                    marginBottom: "0.75rem",
                                    color: "#b00020",
                                    fontSize: "0.85rem",
                                }}
                            >
                                {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="navbar-btn"
                        >
                            {loading ? "Logging in..." : "Log in as practitioner"}
                        </button>
                    </form>

                    <button
                        type="button"
                        onClick={() => navigate("/register-practitioner")}
                        className="navbar-btn"

                    >
                        Create account
                    </button>

                </div>


            </div>


        </div>
    );
}

export default LoginPage;
