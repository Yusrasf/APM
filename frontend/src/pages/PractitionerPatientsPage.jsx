// src/pages/PractitionerPatientsPage.jsx
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchMyPatients } from "../api/practitionerApi";

function PractitionerPatientsPage() {
    const [patients, setPatients] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        fetchMyPatients().then(setPatients).catch(console.error);
    }, []);

    const handleRowClick = (id) => {
        navigate(`/practitioner/patients/${id}`);
    };

    return (
        <div style={{ padding: "2rem" }}>
            <h1>My Patients</h1>
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                <tr>
                    <th align="left">ID</th>
                    <th align="left">Name</th>
                    <th align="left">Birth date</th>
                </tr>
                </thead>
                <tbody>
                {patients.map((p) => (
                    <tr
                        key={p.patientId}
                        style={{ cursor: "pointer" }}
                        onClick={() => handleRowClick(p.patientId)}
                    >
                        <td>{p.patientId}</td>
                        <td>
                            {p.firstName} {p.lastName}
                        </td>
                        <td>{p.birthDate}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default PractitionerPatientsPage;
