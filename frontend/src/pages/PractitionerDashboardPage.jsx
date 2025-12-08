import React, { useEffect, useState } from "react";
import { practitionerApi } from "../api/practitionerApi";
import { getAuth, logout } from "../api/authApi";
import { useNavigate } from "react-router-dom";

function PractitionerDashboardPage() {
    const navigate = useNavigate();
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const auth = getAuth();
        if (!auth) {
            navigate("/");
            return;
        }

        async function loadPatients() {
            try {
                setLoading(true);
                const res = await practitionerApi.getMyPatients();
                setPatients(res.data);
            } catch (err) {
                console.error(err);
                setError("Failed to load patients.");
            } finally {
                setLoading(false);
            }
        }

        loadPatients();
    }, [navigate]);

    const handleLogout = () => {
        logout();
        navigate("/");
    };

    if (loading) return <div style={{ padding: "2rem" }}>Loading...</div>;

    return (
        <div style={{ padding: "2rem" }}>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
                <h2>My Patients</h2>
                <button onClick={handleLogout}>Logout</button>
            </div>

            {error && (
                <div style={{ color: "#b00020", marginBottom: "1rem" }}>{error}</div>
            )}

            <table
                style={{
                    borderCollapse: "collapse",
                    width: "100%",
                    marginTop: "1rem",
                }}
            >
                <thead>
                <tr>
                    <th style={{ borderBottom: "1px solid #ccc", padding: "0.5rem" }}>
                        ID
                    </th>
                    <th style={{ borderBottom: "1px solid #ccc", padding: "0.5rem" }}>
                        Name
                    </th>
                    <th style={{ borderBottom: "1px solid #ccc", padding: "0.5rem" }}>
                        Birth date
                    </th>
                </tr>
                </thead>
                <tbody>
                {patients.map((p) => (
                    <tr key={p.patientId}>
                        <td style={{ borderBottom: "1px solid #eee", padding: "0.5rem" }}>
                            {p.patientId}
                        </td>
                        <td style={{ borderBottom: "1px solid #eee", padding: "0.5rem" }}>
                            {p.firstName} {p.lastName}
                        </td>
                        <td style={{ borderBottom: "1px solid #eee", padding: "0.5rem" }}>
                            {p.birthDate}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default PractitionerDashboardPage;
