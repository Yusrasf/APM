import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

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

const buttonStyle = {
    width: "100%",
    padding: "0.7rem",
    borderRadius: "6px",
    border: "none",
    cursor: "pointer",
    fontWeight: 600,
    fontSize: "0.95rem",
};

function LoginPage() {
    const navigate = useNavigate();
    const [practitionerUsername, setPractitionerUsername] = useState("dr.smith");
    const [practitionerPassword, setPractitionerPassword] = useState(
        "password123"
    );
    const [error, setError] = useState("");

    const handlePractitionerLogin = (e) => {
        e.preventDefault();
        if (!practitionerUsername || !practitionerPassword) {
            setError("Please enter username and password.");
            return;
        }

        localStorage.setItem(
            "practitionerAuth",
            JSON.stringify({
                username: practitionerUsername,
                password: practitionerPassword,
            })
        );
        setError("");
        navigate("/practitioner");
    };

    return (
        <div style={containerStyle}>
            {/* Left side – Parent login placeholder */}
            <div style={leftStyle}>
                <div style={cardStyle}>
                    <h1 style={{ marginBottom: "0.5rem" }}>
                        Vaccination Tracking System
                    </h1>
                    <p style={{ marginBottom: "2rem", color: "#555" }}>
                        For parents to see their children&apos;s immunizations and upcoming
                        vaccination appointments.
                    </p>

                    <h2 style={{ marginBottom: "0.5rem" }}>Parent login</h2>
                    <p style={{ marginBottom: "1rem", fontSize: "0.9rem", color: "#666" }}>
                        This part will be wired later. For now, use the practitioner login
                        on the right.
                    </p>

                    <div
                        style={{
                            padding: "1rem",
                            borderRadius: "8px",
                            background: "#e9f4ff",
                            fontSize: "0.9rem",
                            color: "#225",
                        }}
                    >
                        <strong>Demo:</strong> Practitioner login:{" "}
                        <code>dr.smith / password123</code>
                    </div>
                </div>
            </div>

            {/* Right side – Practitioner login */}
            <div style={rightStyle}>
                <div style={cardStyle}>
                    <h2 style={{ marginBottom: "0.5rem" }}>Practitioner login</h2>
                    <p style={{ marginBottom: "1.5rem", fontSize: "0.9rem", color: "#666" }}>
                        Log in to see your assigned patients and manage vaccinations.
                    </p>

                    <form onSubmit={handlePractitionerLogin}>
                        <label style={{ fontSize: "0.85rem", fontWeight: 600 }}>
                            Username
                        </label>
                        <input
                            style={inputStyle}
                            value={practitionerUsername}
                            onChange={(e) => setPractitionerUsername(e.target.value)}
                            placeholder="dr.smith"
                        />

                        <label style={{ fontSize: "0.85rem", fontWeight: 600 }}>
                            Password
                        </label>
                        <input
                            style={inputStyle}
                            type="password"
                            value={practitionerPassword}
                            onChange={(e) => setPractitionerPassword(e.target.value)}
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
                            style={{ ...buttonStyle, background: "#2563eb", color: "#fff" }}
                        >
                            Log in as practitioner
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;
