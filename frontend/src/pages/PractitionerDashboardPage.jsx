// src/pages/PractitionerDashboardPage.jsx
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchMyPatients } from "../api/practitionerApi";

export default function PractitionerDashboardPage() {
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);

    const navigate = useNavigate();
    const practitioner = JSON.parse(localStorage.getItem("practitioner"));

    useEffect(() => {
        fetchMyPatients()
            .then(setPatients)
            .finally(() => setLoading(false));
    }, []);

    const logout = () => {
        localStorage.clear();
        navigate("/login");
    };

    const goToPatient = (id) => navigate(`/practitioner/patients/${id}`);

    return (
        <div style={{ padding: 32 }}>
            <header style={{ display: "flex", justifyContent: "space-between" }}>
                <div>
                    <h1>Practitioner Dashboard</h1>
                    {practitioner && (
                        <p>
                            Welcome <strong>{practitioner.fullName}</strong> ·{" "}
                            {practitioner.organizationName}
                        </p>
                    )}
                </div>

                <button onClick={logout}>Logout</button>
            </header>

            <h2>My Patients</h2>

            {loading ? (
                <p>Loading…</p>
            ) : (
                <table style={{ width: "100%", marginTop: 16 }}>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Birth date</th>
                    </tr>
                    </thead>
                    <tbody>
                    {patients.map((p) => (
                        <tr
                            key={p.patientId}
                            style={{ cursor: "pointer" }}
                            onClick={() => goToPatient(p.patientId)}
                        >
                            <td>{p.patientId}</td>
                            <td>{p.firstName} {p.lastName}</td>
                            <td>{p.birthDate}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
