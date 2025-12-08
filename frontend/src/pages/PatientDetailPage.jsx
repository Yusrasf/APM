import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
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

const gridStyle = {
    display: "grid",
    gridTemplateColumns: "2fr 3fr",
    gap: "1rem",
    alignItems: "start",
};

const cardStyle = {
    background: "#ffffff",
    borderRadius: "10px",
    padding: "1rem 1.25rem",
    boxShadow: "0 1px 3px rgba(15,23,42,0.08)",
    marginBottom: "1rem",
};

function PatientDetailPage() {
    const { patientId } = useParams();
    const navigate = useNavigate();

    const [overview, setOverview] = useState(null);
    const [immunizations, setImmunizations] = useState([]);
    const [recommendations, setRecommendations] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                setError("");

                const [overviewRes, immRes, recRes, apptRes] = await Promise.all([
                    practitionerApi.getPatientOverview(patientId),
                    practitionerApi.getImmunizations(patientId),
                    practitionerApi.getRecommendations(patientId),
                    practitionerApi.getAppointments(patientId),
                ]);

                setOverview(overviewRes.data);
                setImmunizations(immRes.data || []);
                setRecommendations(recRes.data || []);
                setAppointments(apptRes.data || []);
            } catch (err) {
                console.error(err);
                setError("Failed to load patient details.");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [patientId]);

    const patientName =
        overview?.patient?.firstName && overview?.patient?.lastName
            ? `${overview.patient.firstName} ${overview.patient.lastName}`
            : patientId;

    return (
        <div style={containerStyle}>
            <header style={headerStyle}>
                <div>
                    <button
                        onClick={() => navigate("/practitioner")}
                        style={{
                            marginRight: "1rem",
                            border: "none",
                            background: "transparent",
                            color: "#2563eb",
                            cursor: "pointer",
                            fontSize: "0.9rem",
                        }}
                    >
                        ← Back to patients
                    </button>
                    <strong>Patient details</strong>
                </div>
            </header>

            <main style={contentStyle}>
                {loading && <div>Loading…</div>}
                {error && <div style={{ color: "#b00020" }}>{error}</div>}

                {!loading && !error && (
                    <>
                        <h2 style={{ marginBottom: "1rem" }}>{patientName}</h2>
                        <div style={gridStyle}>
                            {/* Left column: basic info & related persons */}
                            <div>
                                <div style={cardStyle}>
                                    <h3>Basic information</h3>
                                    {overview?.patient ? (
                                        <ul
                                            style={{
                                                listStyle: "none",
                                                paddingLeft: 0,
                                                fontSize: "0.9rem",
                                            }}
                                        >
                                            <li>
                                                <strong>Patient ID:</strong> {overview.patient.patientId}
                                            </li>
                                            <li>
                                                <strong>Name:</strong> {overview.patient.firstName}{" "}
                                                {overview.patient.lastName}
                                            </li>
                                            <li>
                                                <strong>Birth date:</strong> {overview.patient.birthDate}
                                            </li>
                                            <li>
                                                <strong>Gender:</strong> {overview.patient.gender}
                                            </li>
                                        </ul>
                                    ) : (
                                        <div>No patient details available.</div>
                                    )}
                                </div>

                                <div style={cardStyle}>
                                    <h3>Related persons</h3>
                                    {overview?.relatedPersons &&
                                    overview.relatedPersons.length > 0 ? (
                                        <ul
                                            style={{
                                                paddingLeft: "1.1rem",
                                                fontSize: "0.9rem",
                                            }}
                                        >
                                            {overview.relatedPersons.map((rp, index) => (
                                                <li key={rp.relatedPersonId || index}>
                                                    {rp.relationship} – {rp.name}{" "}
                                                    {rp.telecom && `(${rp.telecom})`}
                                                </li>
                                            ))}
                                        </ul>
                                    ) : (
                                        <div>No related persons found.</div>
                                    )}
                                </div>
                            </div>

                            {/* Right column: immunizations, recommendations, appointments */}
                            <div>
                                <div style={cardStyle}>
                                    <h3>Immunizations</h3>
                                    {immunizations.length === 0 ? (
                                        <div>No immunizations recorded.</div>
                                    ) : (
                                        <table
                                            style={{
                                                width: "100%",
                                                borderCollapse: "collapse",
                                                fontSize: "0.9rem",
                                            }}
                                        >
                                            <thead>
                                            <tr>
                                                <th
                                                    style={{
                                                        textAlign: "left",
                                                        padding: "0.4rem",
                                                    }}
                                                >
                                                    Vaccine
                                                </th>
                                                <th
                                                    style={{
                                                        textAlign: "left",
                                                        padding: "0.4rem",
                                                    }}
                                                >
                                                    Date
                                                </th>
                                                <th
                                                    style={{
                                                        textAlign: "left",
                                                        padding: "0.4rem",
                                                    }}
                                                >
                                                    Status
                                                </th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            {immunizations.map((imm) => (
                                                <tr key={imm.immunizationId}>
                                                    <td style={{ padding: "0.4rem" }}>
                                                        {imm.vaccineDisplay || imm.vaccineCode}
                                                    </td>
                                                    <td style={{ padding: "0.4rem" }}>
                                                        {imm.occurrenceDateTime}
                                                    </td>
                                                    <td style={{ padding: "0.4rem" }}>{imm.status}</td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
                                    )}
                                </div>

                                <div style={cardStyle}>
                                    <h3>Immunization recommendations</h3>
                                    {recommendations.length === 0 ? (
                                        <div>No recommendations.</div>
                                    ) : (
                                        <ul
                                            style={{
                                                paddingLeft: "1.1rem",
                                                fontSize: "0.9rem",
                                            }}
                                        >
                                            {recommendations.map((rec) => (
                                                <li key={rec.id}>
                                                    <strong>
                                                        {rec.vaccineDisplay || rec.vaccineCode}
                                                    </strong>{" "}
                                                    – due: {rec.dueDate || "n/a"} – status:{" "}
                                                    {rec.status || "n/a"}{" "}
                                                    {rec.doseNumber && `(dose ${rec.doseNumber})`}
                                                </li>
                                            ))}
                                        </ul>
                                    )}
                                </div>

                                <div style={cardStyle}>
                                    <h3>Appointments</h3>
                                    {appointments.length === 0 ? (
                                        <div>No appointments.</div>
                                    ) : (
                                        <ul
                                            style={{
                                                paddingLeft: "1.1rem",
                                                fontSize: "0.9rem",
                                            }}
                                        >
                                            {appointments.map((appt) => (
                                                <li key={appt.id}>
                                                    {appt.start} – {appt.status} –{" "}
                                                    {appt.reason || "no reason"}
                                                </li>
                                            ))}
                                        </ul>
                                    )}
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </main>
        </div>
    );
}

export default PatientDetailPage;
