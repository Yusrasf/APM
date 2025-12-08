import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage.jsx";
import PractitionerDashboardPage from "./pages/PractitionerDashboardPage.jsx";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LoginPage />} />
                <Route path="/practitioner" element={<PractitionerDashboardPage />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
