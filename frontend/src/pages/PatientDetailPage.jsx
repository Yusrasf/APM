// src/pages/PatientDetailPage.jsx
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
    fetchPatientOverview,
    fetchImmunizations,
    createImmunization,
    fetchRecommendations,
    createRecommendation,
    fetchAppointments,
    createAppointment,
    updateAppointmentStatus,
} from "../api/practitionerApi";

function PatientDetailPage() {
    const { patientId } = useParams();

    const [overview, setOverview] = useState(null);
    const [immunizations, setImmunizations] = useState([]);
    const [recommendations, setRecommendations] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [activeTab, setActiveTab] = useState("overview");

    // simple form state
    const [immForm, setImmForm] = useState({
        vaccineCode: "",
        vaccineDisplay: "",
        date: "",
        lotNumber: "",
    });

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
        // load all blocks for this patient
        fetchPatientOverview(patientId).then(setOverview).catch(console.error);
        fetchImmunizations(patientId).then(setImmunizations).catch(console.error);
        fetchRecommendations(patientId)
            .then(setRecommendations)
            .catch(console.error);
        fetchAppointments(patientId).then(setAppointments).catch(console.error);
    }, [patientId]);

    const handleCreateImmunization = async (e) => {
        e.preventDefault();
        const payload = {
            vaccineCode: immForm.vaccineCode,
            vaccineDisplay: immForm.vaccineDisplay,
            lotNumber: immForm.lotNumber || null,
            date: immForm.date || null, // LocalDate string "2025-01-01"
        };
        const created = await createImmunization(patientId, payload);
        setImmunizations((prev) => [...prev, created]);
        setImmForm({ vaccineCode: "", vaccineDisplay: "", date: "", lotNumber: "" });
    };

    const handleCreateRecommendation = async (e) => {
        e.preventDefault();
        const payload = {
            vaccineCode: recForm.vaccineCode,
            vaccineDisplay: recForm.vaccineDisplay,
            dueDate: recForm.dueDate || null,
            series: recForm.series || null,
            doseNumber: recForm.doseNumber
                ? parseInt(recForm.doseNumber, 10)
                : null,
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
            start: apptForm.start ? apptForm.start + ":00" : null, // "2025-01-10T09:00"
            end: apptForm.end ? apptForm.end + ":00" : null,
            reason: apptForm.reason || null,
            location: apptForm.location || null,
        };
        const created = await createAppointment(payload);
        setAppointments((prev) => [...prev, created]);
        setApptForm({ start: "", end: "", reason: "", location: "" });
    };

    const handleUpdateAppointmentStatus = async (id, status) => {
        const updated = await updateAppointmentStatus(id, status);
        setAppointments((prev) =>
            prev.map((a) => (a.id === id ? updated : a))
        );
    };

    if (!overview) {
        return <div style={{ padding: "2rem" }}>Loading...</div>;
    }

    const patient = overview.patient;

    return (
        <div style={{ padding: "2rem" }}>
            <h1>
                {patient.fullName} ({patient.patientId})
            </h1>
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
                    <h2>Clinical overview</h2>

                    {/* ENCOUNTERS */}
                    <div style={{ marginTop: "2rem" }}>
                        <h3>Encounters</h3>

                        {overview.encounters && overview.encounters.length > 0 ? (
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
                                        <strong>Encounter ID:</strong> {enc.encounter.encounterId} <br/>
                                        <strong>Status:</strong> {enc.encounter.status}
                                    </p>

                                    {/* Location */}
                                    {enc.location && (
                                        <p>
                                            <strong>Location:</strong> {enc.location.name} <br/>
                                            <strong>Managing organization:</strong> {enc.organization?.name}
                                        </p>
                                    )}

                                    {/* Immunizations */}
                                    <div style={{ marginTop: "1rem" }}>
                                        <strong>Immunizations:</strong>
                                        {enc.immunizations && enc.immunizations.length > 0 ? (
                                            <ul>
                                                {enc.immunizations.map((imm) => (
                                                    <li key={imm.immunization.immunizationId}>
                                                        {imm.immunization.occurrenceDateTime} – {imm.immunization.vaccineDisplay}
                                                        {imm.practitioner && (
                                                            <> (by Dr. {imm.practitioner.firstName} {imm.practitioner.lastName})</>
                                                        )}
                                                    </li>
                                                ))}
                                            </ul>
                                        ) : (
                                            <p>No immunizations in this encounter.</p>
                                        )}
                                    </div>

                                    {/* Observations */}
                                    <div style={{ marginTop: "1rem" }}>
                                        <strong>Observations:</strong>
                                        {enc.observations && enc.observations.length > 0 ? (
                                            <ul>
                                                {enc.observations.map((obs) => (
                                                    <li key={obs.observationId}>
                                                        {obs.display}: {obs.value}
                                                        {obs.unit && <> {obs.unit}</>}
                                                        {" "}
                                                        ({obs.effectiveDateTime})
                                                    </li>
                                                ))}
                                            </ul>
                                        ) : (
                                            <p>No observations for this encounter.</p>
                                        )}
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p>No encounters available.</p>
                        )}
                    </div>

                    {/* ALLERGIES */}
                    <div style={{ marginTop: "1rem" }}>
                        <h3>Allergies</h3>
                        {overview.allergies && overview.allergies.length > 0 ? (
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

            {activeTab === "immunizations" && (
                <div>
                    <h2>Immunizations</h2>
                    <ul>
                        {immunizations.map((imm) => (
                            <li key={imm.immunizationId}>
                                {imm.occurrenceDateTime} –  vaccine code CVX : {imm.vaccineCode} | {imm.vaccineDisplay} | status: ({imm.status}) | ID: {imm.immunizationId}
                            </li>
                        ))}
                    </ul>

                    <h3 style={{ marginTop: "1.5rem" }}>Add immunization</h3>
                    <form onSubmit={handleCreateImmunization}>
                        <div>
                            <label>Vaccine code      </label>
                            <input
                                value={immForm.vaccineCode}
                                onChange={(e) =>
                                    setImmForm({ ...immForm, vaccineCode: e.target.value })
                                }
                                required
                            />
                        </div>
                        <p>

                        </p>
                        <div>
                            <label>Vaccine display    </label>
                            <input
                                value={immForm.vaccineDisplay}
                                onChange={(e) =>
                                    setImmForm({ ...immForm, vaccineDisplay: e.target.value })
                                }
                                required
                            />
                        </div>
                        <p>

                        </p>
                        <div>
                            <label>Date (dd.mm.yy) </label>
                            <input
                                type="date"
                                value={immForm.date}
                                onChange={(e) =>
                                    setImmForm({ ...immForm, date: e.target.value })
                                }
                            />
                        </div>
                        <p>

                        </p>
                        <div>
                            <label>Status </label>
                            <input
                                value={immForm.status}
                                onChange={(e) =>
                                    setImmForm({ ...immForm, status: e.target.value })
                                }
                            />
                        </div>
                        <p>

                        </p>
                        <div>
                            <label>Immunization ID </label>
                            <input
                                value={immForm.immunizationId}
                                onChange={(e) =>
                                    setImmForm({ ...immForm, immunizationId: e.target.value })
                                }
                            />
                        </div>
                        <p>

                        </p>
                        <button type="submit">Save immunization</button>
                    </form>
                </div>
            )}

            {activeTab === "recommendations" && (
                <div>
                    <h2>Immunization recommendations</h2>
                    <ul>
                        {recommendations.map((r) => (
                            <li key={r.id}>
                                {r.vaccineDisplay} – due {r.dueDate} – status: {r.status}
                            </li>
                        ))}
                    </ul>

                    <h3 style={{ marginTop: "1.5rem" }}>Add recommendation</h3>
                    <form onSubmit={handleCreateRecommendation}>
                        <div>
                            <label>Vaccine code</label>
                            <input
                                value={recForm.vaccineCode}
                                onChange={(e) =>
                                    setRecForm({ ...recForm, vaccineCode: e.target.value })
                                }
                                required
                            />
                        </div>
                        <div>
                            <label>Vaccine display</label>
                            <input
                                value={recForm.vaccineDisplay}
                                onChange={(e) =>
                                    setRecForm({ ...recForm, vaccineDisplay: e.target.value })
                                }
                                required
                            />
                        </div>
                        <div>
                            <label>Due date</label>
                            <input
                                type="date"
                                value={recForm.dueDate}
                                onChange={(e) =>
                                    setRecForm({ ...recForm, dueDate: e.target.value })
                                }
                            />
                        </div>
                        <div>
                            <label>Series</label>
                            <input
                                value={recForm.series}
                                onChange={(e) =>
                                    setRecForm({ ...recForm, series: e.target.value })
                                }
                            />
                        </div>
                        <div>
                            <label>Dose number</label>
                            <input
                                type="number"
                                value={recForm.doseNumber}
                                onChange={(e) =>
                                    setRecForm({ ...recForm, doseNumber: e.target.value })
                                }
                            />
                        </div>
                        <div>
                            <label>Notes</label>
                            <textarea
                                value={recForm.notes}
                                onChange={(e) =>
                                    setRecForm({ ...recForm, notes: e.target.value })
                                }
                            />
                        </div>
                        <button type="submit">Save recommendation</button>
                    </form>
                </div>
            )}

            {activeTab === "appointments" && (
                <div>
                    <h2>Appointments</h2>
                    <ul>
                        {appointments.map((a) => (
                            <li key={a.id}>
                                {a.start} – {a.reason} – status {a.status}{" "}
                                <button
                                    onClick={() => handleUpdateAppointmentStatus(a.id, "cancelled")}
                                >
                                    Cancel
                                </button>
                            </li>
                        ))}
                    </ul>

                    <h3 style={{ marginTop: "1.5rem" }}>Create appointment</h3>
                    <form onSubmit={handleCreateAppointment}>
                        <div>
                            <label>Start (YYYY-MM-DDTHH:MM)</label>
                            <input
                                type="datetime-local"
                                value={apptForm.start}
                                onChange={(e) =>
                                    setApptForm({ ...apptForm, start: e.target.value })
                                }
                            />
                        </div>
                        <div>
                            <label>End (YYYY-MM-DDTHH:MM)</label>
                            <input
                                type="datetime-local"
                                value={apptForm.end}
                                onChange={(e) =>
                                    setApptForm({ ...apptForm, end: e.target.value })
                                }
                            />
                        </div>
                        <div>
                            <label>Reason</label>
                            <input
                                value={apptForm.reason}
                                onChange={(e) =>
                                    setApptForm({ ...apptForm, reason: e.target.value })
                                }
                            />
                        </div>
                        <div>
                            <label>Location</label>
                            <input
                                value={apptForm.location}
                                onChange={(e) =>
                                    setApptForm({ ...apptForm, location: e.target.value })
                                }
                            />
                        </div>
                        <button type="submit">Save appointment</button>
                    </form>
                </div>
            )}
        </div>
    );
}

export default PatientDetailPage;
