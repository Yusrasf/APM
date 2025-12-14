// src/pages/RegisterPatientPage.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerPatient } from "../api/practitionerApi";

function RegisterPatientPage() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        identifier: "",
        firstName: "",
        lastName: "",
        birthDate: "",
        gender: "",
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        try {
            await registerPatient(form);
            navigate("/practitioner"); // Go back to patient list
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || "Failed to register patient");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: "2rem", maxWidth: "500px", margin: "0 auto" }}>
            <h2>Register New Patient</h2>
            <form onSubmit={handleSubmit}>
                {/*<div style={{ marginBottom: "1rem" }}>
                    <label>Identifier</label>
                    <input
                        required
                        value={form.identifier}
                        onChange={(e) => setForm({ ...form, identifier: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.2rem" }}
                    />
                </div>*/}

                <div style={{ marginBottom: "1rem" }}>
                    <label>First Name</label>
                    <input
                        required
                        value={form.firstName}
                        onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.2rem" }}
                    />
                </div>

                <div style={{ marginBottom: "1rem" }}>
                    <label>Last Name</label>
                    <input
                        required
                        value={form.lastName}
                        onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.2rem" }}
                    />
                </div>

                <div style={{ marginBottom: "1rem" }}>
                    <label>Birth Date</label>
                    <input
                        type="date"
                        required
                        value={form.birthDate}
                        onChange={(e) => setForm({ ...form, birthDate: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.2rem" }}
                    />
                </div>

                <div style={{ marginBottom: "1.5rem" }}>
                    <label>Gender</label>
                    <select
                        required
                        value={form.gender}
                        onChange={(e) => setForm({ ...form, gender: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.2rem" }}
                    >
                        <option value="">Select</option>
                        <option value="male">Male</option>
                        <option value="female">Female</option>
                        <option value="other">Other</option>
                        <option value="unknown">Unknown</option>
                    </select>
                </div>

                {error && <div style={{ color: "#b00020", marginBottom: "2rem" }}>{error}</div>}

                <button
                    type="submit"
                    disabled={loading}
                    className="navbar-btn navbar-btn-primary"
                >
                    {loading ? "Registering..." : "Register Patient"}
                </button>
            </form>
        </div>
    );
}

export default RegisterPatientPage;