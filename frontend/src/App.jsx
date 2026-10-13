import { Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Nav from "./components/Nav";
import ProtectedRoute from "./components/ProtectedRoute";
import Dashboard from "./pages/Dashboard";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Profile from "./pages/Profile";
import History from "./pages/History";
import MapPage from "./pages/MapPage";
import Reports from "./pages/Reports";
import Notifications from "./pages/Notifications";
import AdminSources from "./pages/AdminSources";

export default function App() {
  return (
    <AuthProvider>
      <Nav />
      <main className="main">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/history" element={<ProtectedRoute><History /></ProtectedRoute>} />
          <Route path="/map" element={<ProtectedRoute><MapPage /></ProtectedRoute>} />
          <Route path="/reports" element={<ProtectedRoute><Reports /></ProtectedRoute>} />
          <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
          <Route
            path="/admin"
            element={<ProtectedRoute adminOnly><AdminSources /></ProtectedRoute>}
          />
        </Routes>
      </main>
    </AuthProvider>
  );
}