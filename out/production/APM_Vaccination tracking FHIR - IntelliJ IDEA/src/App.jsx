import React from "react";
import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage.jsx";
import PractitionerDashboardPage from "./pages/PractitionerDashboardPage.jsx";
import PatientDetailPage from "./pages/PatientDetailPage.jsx";

function App() {
    return (
        <Routes>
            <Route path="/" element={<LoginPage />} />
            <Route path="/practitioner" element={<PractitionerDashboardPage />} />
            <Route
                path="/practitioner/patients/:patientId"
                element={<PatientDetailPage />}
            />
        </Routes>
    );
}

export default App;
