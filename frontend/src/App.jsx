import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './AuthContext';
import LoginPage from './LoginPage';
import WorkspacesPage from './WorkspacesPage';
import WorkspacePage from './WorkspacePage';
import RegisterPage from './RegisterPage';
import Layout from './Layout';


function ProtectedRoute({ children }) {
    const { token } = useAuth();
    return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                <Route path="/" element={<WorkspacesPage />} />
                <Route path="/w/:workspaceId" element={<WorkspacePage />} />
            </Route>
        </Routes>
    );
}