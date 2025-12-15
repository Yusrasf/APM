// src/pages/PatientDetailPage.jsx
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
    fetchPatientOverview,
    fetchImmunizations,
    fetchRecommendations,
    createRecommendation,
    fetchAppointments,
    createAppointment,
    updateAppointmentStatus,
} from "../api/practitionerApi";

import FHIRAdminPanel from "../components/FHIRAdminPanel";

function PatientDetailPage() {
    const { patientId } = useParams();

    const [overview, setOverview] = useState(null);
    const [immunizations, setImmunizations] = useState([]);
    const [recommendations, setRecommendations] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [activeTab, setActiveTab] = useState("overview");

    const [recForm, setRecForm] = useState({
        vaccineCode: "",
        vaccineDisplay: "",
        dueDate: "",
        series: "",
        doseNumber: "",
        notes: "",
    });

    const [apptForm, setApptForm] = useState({
        start: "",
        end: "",
        reason: "",
        location: "",
    });

    useEffect(() => {
        fetchPatientOverview(patientId).then(setOverview).catch(console.error);
        fetchImmunizations(patientId).then(setImmunizations).catch(console.error);
        fetchRecommendations(patientId).then(setRecommendations).catch(console.error);
        fetchAppointments(patientId).then(setAppointments).catch(console.error);
    }, [patientId]);

    if (!overview) {
        return <div style={{ padding: "2rem" }}>Loading...</div>;
    }

    const handleCreateRecommendation = async (e) => {
        e.preventDefault();
        const payload = {
            vaccineCode: recForm.vaccineCode,
            vaccineDisplay: recForm.vaccineDisplay,
            dueDate: recForm.dueDate || null,
            series: recForm.series || null,
            doseNumber: recForm.doseNumber ? parseInt(recForm.doseNumber) : null,
            notes: recForm.notes || null,
        };

        const created = await createRecommendation(patientId, payload);
        setRecommendations((prev) => [...prev, created]);

        setRecForm({
            vaccineCode: "",
            vaccineDisplay: "",
            dueDate: "",
            series: "",
            doseNumber: "",
            notes: "",
        });
    };

    const handleCreateAppointment = async (e) => {
        e.preventDefault();

        const payload = {
            patientId,
            start: apptForm.start ? apptForm.start + ":00" : null,
            end: apptForm.end ? apptForm.end + ":00" : null,
            reason: apptForm.reason || null,
            location: apptForm.location || null,
        };

        const created = await createAppointment(payload);
        setAppointments((prev) => [...prev, created]);

        setApptForm({ start: "", end: "", reason: "", location: "" });
    };

    const patient = overview.patient;

    return (
        <div style={{ padding: "2rem",  background: "linear-gradient(135deg, #e8f1ff, #FFFBDE)",
            minHeight: "100vh" }}>
            <h1>
                <strong>ID: </strong>{patient.fullName} ({patient.patientId})
            </h1>
            <p>
                <strong>First name: </strong>{patient.firstName} &nbsp; | &nbsp;
                <strong>Last name: </strong> {patient.lastName}
            </p>
            <p>
                <strong>Birth date:</strong> {patient.birthDate} &nbsp; | &nbsp;
                <strong>Gender:</strong> {patient.gender}
            </p>

            {/* Tabs */}
            <div style={{ marginTop: "2rem", marginBottom: "1rem" }}>
                {["overview", "immunizations", "recommendations", "appointments"].map(
                    (tab) => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            style={{
                                marginRight: "0.5rem",
                                padding: "0.5rem 1rem",
                                borderRadius: "999px",
                                border:
                                    activeTab === tab ? "2px solid #2563eb" : "1px solid #ddd",
                                background: activeTab === tab ? "#eff6ff" : "white",
                                cursor: "pointer",
                            }}
                        >
                            {tab[0].toUpperCase() + tab.slice(1)}
                        </button>
                    )
                )}
            </div>

            {/* CONTENT BY TAB */}
            {activeTab === "overview" && (
                <div>
                    <h2>Patient Clinical Dasboard - Immunizations overview</h2>

                    {/* Encounters */}
                    <div style={{ marginTop: "2rem" }}>
                        <h3>Encounters</h3>

                        {overview.encounters?.length > 0 ? (
                            overview.encounters.map((enc) => (
                                <div
                                    key={enc.encounter.encounterId}
                                    style={{
                                        border: "1px solid #ddd",
                                        padding: "1rem",
                                        borderRadius: 8,
                                        marginBottom: "1rem",
                                        background: "#fafafa"
                                    }}
                                >
                                    <p>
                                        <strong>Encounter ID:</strong> {enc.encounter.encounterId}<br />
                                        <strong>Status:</strong> {enc.encounter.status}
                                    </p>

                                    {enc.location && (
                                        <p>
                                            <strong>Location:</strong> {enc.location.name}<br />
                                            <strong>Managing organization:</strong> {enc.organization?.name}
                                        </p>
                                    )}

                                    <div style={{ marginTop: "1rem" }}>
                                        <strong>Immunizations:</strong>
                                        {enc.immunizations.length > 0 ? (
                                            <ul>
                                                {enc.immunizations.map((imm) => (
                                                    <li key={imm.immunization.immunizationId}>
                                                        {imm.immunization.occurrenceDateTime} –
                                                        {imm.immunization.vaccineDisplay}
                                                    </li>
                                                ))}
                                            </ul>
                                        ) : (
                                            <p>No immunizations in this encounter.</p>
                                        )}
                                    </div>

                                </div>
                            ))
                        ) : (
                            <p>No encounters available.</p>
                        )}
                    </div>

                    {/* Allergies */}
                    <div style={{ marginTop: "1rem" }}>
                        <h3>Allergies</h3>
                        {overview.allergies?.length > 0 ? (
                            <ul>
                                {overview.allergies.map((a) => (
                                    <li key={a.allergyId}>
                                        <strong>{a.display}</strong>
                                        {a.reaction && <> – reaction: {a.reaction}</>}
                                        {a.criticality && <> – criticality: {a.criticality}</>}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p>No allergies recorded.</p>
                        )}
                    </div>
                </div>
            )}

            {/* IMMUNIZATIONS TAB */}
            {activeTab === "immunizations" && (
                <div style={{ padding: 15, border: "5px solid #ccc", background: "#fafafa"}}>
                    <h2>Immunizations History: </h2>

                    <ul>
                        {immunizations.map((imm) => (
                            <li key={imm.immunizationId}>
                                CVX: {imm.vaccineCode} | {imm.vaccineDisplay} | ID: {imm.immunizationId} | <i>effective date:</i> {imm.occurrenceDateTime}
                            </li>
                        ))}
                    </ul>

                    {/* HERE WE INSERT THE FHIR PUT PANEL */}
                    <FHIRAdminPanel patientId={patientId} />
                </div>
            )}

            {/* RECOMMENDATIONS TAB */}
            {activeTab === "recommendations" && (
                <div>
                    <h2>Immunization recommendations</h2>
                    <ul>
                        {recommendations.map((r) => (
                            <li key={r.id}>
                                {r.vaccineDisplay} – due {r.dueDate} – status {r.status}
                            </li>
                        ))}
                    </ul>

                    <h3 style={{ marginTop: "1.5rem" }}>Add recommendation</h3>

                    <form onSubmit={handleCreateRecommendation}>
                        <label>Vaccine code</label>
                        <input
                            value={recForm.vaccineCode}
                            onChange={(e) => setRecForm({ ...recForm, vaccineCode: e.target.value })}
                            required
                        />

                        <label>Vaccine display</label>
                        <input
                            value={recForm.vaccineDisplay}
                            onChange={(e) => setRecForm({ ...recForm, vaccineDisplay: e.target.value })}
                            required
                        />

                        <label>Due date</label>
                        <input
                            type="date"
                            value={recForm.dueDate}
                            onChange={(e) => setRecForm({ ...recForm, dueDate: e.target.value })}
                        />

                        <button type="submit">Save recommendation</button>
                    </form>
                    <div>
                        <h3>Consent - Immunization recommendations:</h3>

                        {/* To add the consent here */}
                    </div>
                </div>


            )}

            {/* APPOINTMENTS TAB */}
            {activeTab === "appointments" && (
                <div>
                    <h2>Appointments</h2>

                    <ul>
                        {appointments.map((a) => (
                            <li key={a.id}>
                                {a.start} – {a.reason} – status {a.status}
                            </li>
                        ))}
                    </ul>

                    <h3>Create appointment</h3>

                    <form onSubmit={handleCreateAppointment}>
                        <label>Start</label>
                        <input
                            type="datetime-local"
                            value={apptForm.start}
                            onChange={(e) => setApptForm({ ...apptForm, start: e.target.value })}
                        />

                        <label>End</label>
                        <input
                            type="datetime-local"
                            value={apptForm.end}
                            onChange={(e) => setApptForm({ ...apptForm, end: e.target.value })}
                        />

                        <label>Reason</label>
                        <input
                            value={apptForm.reason}
                            onChange={(e) => setApptForm({ ...apptForm, reason: e.target.value })}
                        />

                        <button type="submit">Save appointment</button>
                    </form>
                </div>
            )}
        </div>
    );
}

export default PatientDetailPage;
