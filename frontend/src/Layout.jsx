import { Link, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function Layout() {
    const { user, logout } = useAuth();

    return (
        <>
            <header className="topbar">
                <Link to="/" className="brand">Workspace</Link>
                <div className="topbar-user">
                    <span>{user?.name}</span>
                    <button className="ghost on-dark" onClick={logout}>Αποσύνδεση</button>
                </div>
            </header>
            <main className="page">
                <Outlet />
            </main>
        </>
    );
}