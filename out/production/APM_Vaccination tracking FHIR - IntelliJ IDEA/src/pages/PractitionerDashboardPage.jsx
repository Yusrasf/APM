import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { practitionerApi } from "../api/practitionerApi";

const containerStyle = {
    minHeight: "100vh",
    background: "#f5f7fb",
};

const headerStyle = {
    padding: "1rem 2rem",
    background: "#ffffff",
    borderBottom: "1px solid #e0e0e0",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
};

const contentStyle = {
    padding: "1.5rem 2rem",
};

const cardStyle = {
    background: "#ffffff",
    borderRadius: "10px",
    padding: "1rem 1.25rem",
    boxShadow: "0 1px 3px rgba(15,23,42,0.08)",
};

function PractitionerDashboardPage() {
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                setError("");
                const res = await practitionerApi.getPatients();
                setPatients(res.data || []);
            } catch (err) {
                console.error(err);
                setError("Failed to load patients. Check backend / credentials.");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    const handleLogout = () => {
        localStorage.removeItem("practitionerAuth");
        navigate("/");
    };

    const handleOpenPatient = (patientId) => {
        navigate(`/practitioner/patients/${patientId}`);
    };

    return (
        <div style={containerStyle}>
            <header style={headerStyle}>
                <div>
                    <strong>Vaccination Tracking – Practitioner</strong>
                </div>
                <button
                    onClick={handleLogout}
                    style={{
                        border: "none",
                        padding: "0.5rem 0.9rem",
                        borderRadius: "6px",
                        background: "#e11d48",
                        color: "#fff",
                        cursor: "pointer",
                        fontSize: "0.85rem",
                        fontWeight: 600,
                    }}
                >
                    Log out
                </button>
            </header>

            <main style={contentStyle}>
                <div style={cardStyle}>
                    <h2 style={{ marginBottom: "1rem" }}>My patients</h2>

                    {loading && <div>Loading patients…</div>}
                    {error && (
                        <div style={{ color: "#b00020", marginBottom: "0.75rem" }}>
                            {error}
                        </div>
                    )}

                    {!loading && !error && patients.length === 0 && (
                        <div>No patients found for this practitioner.</div>
                    )}

                    {!loading && !error && patients.length > 0 && (
                        <table
                            style={{
                                width: "100%",
                                borderCollapse: "collapse",
                                fontSize: "0.9rem",
                            }}
                        >
                            <thead>
                            <tr>
                                <th style={{ textAlign: "left", padding: "0.5rem" }}>Name</th>
                                <th style={{ textAlign: "left", padding: "0.5rem" }}>
                                    Birth date
                                </th>
                                <th style={{ textAlign: "left", padding: "0.5rem" }}>
                                    Gender
                                </th>
                                <th style={{ padding: "0.5rem" }}></th>
                            </tr>
                            </thead>
                            <tbody>
                            {patients.map((p) => (
                                <tr key={p.patientId}>
                                    <td style={{ padding: "0.5rem" }}>
                                        {p.firstName} {p.lastName}
                                    </td>
                                    <td style={{ padding: "0.5rem" }}>{p.birthDate}</td>
                                    <td style={{ padding: "0.5rem" }}>{p.gender}</td>
                                    <td style={{ padding: "0.5rem", textAlign: "right" }}>
                                        <button
                                            onClick={() => handleOpenPatient(p.patientId)}
                                            style={{
                                                border: "none",
                                                padding: "0.35rem 0.8rem",
                                                borderRadius: "6px",
                                                background: "#2563eb",
                                                color: "#fff",
                                                cursor: "pointer",
                                                fontSize: "0.85rem",
                                                fontWeight: 500,
                                            }}
                                        >
                                            Open details
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </main>
        </div>
    );
}

export default PractitionerDashboardPage;
