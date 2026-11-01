import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import ClientProfile from "./pages/ClientProfile";
import Orders from "./pages/Orders";
import CustomerDashboard from "./pages/CustomerDashboard";
import NewOrder from "./pages/NewOrder";

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/resetpassword" element={<ResetPassword />} />
        <Route path="/cliente" element={<ClientProfile />} />
        <Route path="/pedidos" element={<Orders />} />
        <Route path="/pedidos/novo" element={<NewOrder />} />
        <Route path="/dashboard" element={<CustomerDashboard />} />
      </Routes>
    </Router>
  );
}