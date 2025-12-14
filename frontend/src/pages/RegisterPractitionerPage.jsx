import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerPractitioner } from "../api/authApi";

function RegisterPractitionerPage() {
    const navigate = useNavigate();
    const [identifier, setIdentifier] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        if (!identifier || !password) {
            setError("Identifier and password are required");
            return;
        }

        try {
            await registerPractitioner(identifier, password);
            setSuccess(true);

            // Redirect back to login after short delay
            setTimeout(() => navigate("/login"), 1200);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Registration failed"
            );
        }
    };

    return (
        <div style={{ maxWidth: 400, margin: "4rem auto" }}>
            <h2>Create Practitioner Account</h2>

            <form onSubmit={handleSubmit}>
                <input
                    placeholder="Identifier"
                    value={identifier}
                    onChange={(e) => setIdentifier(e.target.value)}
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                {error && <p style={{ color: "red" }}>{error}</p>}
                {success && <p style={{ color: "green" }}>Account created</p>}

                <button type="submit" className="navbar-btn navbar-btn-primary">Create account</button>
            </form>
        </div>
    );
}

export default RegisterPractitionerPage;
