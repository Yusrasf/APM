import { useNavigate } from "react-router-dom";
import "./NavBar.css";

function NavBar() {
    const navigate = useNavigate();
    const practitionerName = localStorage.getItem("practitionerName") || "Practitioner";

    const handleLogout = () => {
        localStorage.clear();
        navigate("/login");
    };

    return (
        <nav className="navbar">
            <div className="navbar-left">
                <button
                    className="navbar-btn navbar-btn-patients"
                    onClick={() => navigate("/practitioner")}
                >
                    👥 Patients
                </button>
                <h2 className="navbar-title">Vaccination Tracking System</h2>
            </div>

            <div className="navbar-right">
                <span className="navbar-practitioner">
                    👨‍⚕️ {practitionerName}
                </span>

                <button
                    className="navbar-btn navbar-btn-primary"
                    onClick={() => navigate("/register-patient")}
                >
                    + Add Patient
                </button>

                <button
                    className="navbar-btn navbar-btn-secondary"
                    onClick={handleLogout}
                >
                    Logout
                </button>
            </div>
        </nav>
    );
}

export default NavBar;