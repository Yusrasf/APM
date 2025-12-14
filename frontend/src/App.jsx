// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import PractitionerPatientsPage from "./pages/PractitionerPatientsPage";
import PatientDetailPage from "./pages/PatientDetailPage";
import NavBar from "./components/NavBar";
import {useEffect, useState} from "react";
import RegisterPatientPage from "./pages/RegisterPatientPage.jsx";
import RegisterPractitionerPage from "./pages/RegisterPractitionerPage.jsx";

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    // Check if user is logged in (has token)
    useEffect(() => {
        const token = localStorage.getItem("basicToken");
        setIsLoggedIn(!!token);
    }, []);

    return (
        <BrowserRouter>
            {/* Show NavBar only when logged in */}
            {isLoggedIn && <NavBar />}
            <Routes>
                <Route path="/" element={<Navigate to="/login" />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/practitioner" element={<PractitionerPatientsPage />} />
                <Route
                    path="/practitioner/patients/:patientId"
                    element={<PatientDetailPage />}
                    />
                <Route path="/register-patient" element={<RegisterPatientPage />
                }/>

                <Route
                    path="/register-practitioner"
                    element={<RegisterPractitionerPage />}
                />

            </Routes>
        </BrowserRouter>
    );
}

export default App;
