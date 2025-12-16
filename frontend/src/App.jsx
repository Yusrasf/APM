// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import PractitionerDashboardPage from "./pages/PractitionerDashboardPage";
import PatientDetailPage from "./pages/PatientDetailPage";

function App() {
    return (
        <BrowserRouter>
            <Routes>

                {/* LOGIN → default page */}
                <Route path="/" element={<Navigate to="/login" />} />
                <Route path="/login" element={<LoginPage />} />

                {/* DASHBOARD (My Patients) */}
                <Route path="/practitioner" element={<PractitionerDashboardPage />} />

                {/* PATIENT DETAIL */}
                <Route
                    path="/practitioner/patients/:patientId"
                    element={<PatientDetailPage />}
                />

            </Routes>
        </BrowserRouter>
    );
}

export default App;
