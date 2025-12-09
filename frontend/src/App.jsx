// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import PractitionerPatientsPage from "./pages/PractitionerPatientsPage";
import PatientDetailPage from "./pages/PatientDetailPage";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to="/login" />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/practitioner" element={<PractitionerPatientsPage />} />
                <Route
                    path="/practitioner/patients/:patientId"
                    element={<PatientDetailPage />}
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
