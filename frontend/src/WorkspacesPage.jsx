import { useEffect, useState } from 'react';
import { useAuth } from './AuthContext';
import { api } from './api';
import { Link } from 'react-router-dom';

export default function WorkspacesPage() {
    const { token, logout } = useAuth();
    const [workspaces, setWorkspaces] = useState([]);
    const [name, setName] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        api('/workspaces', { token })
            .then(setWorkspaces)
            .catch((err) => {
                if (err.status === 401) logout();
                else setError(err.message);
            });
    }, [token]);

    async function handleCreate(e) {
        e.preventDefault();
        try {
            const created = await api('/workspaces', { method: 'POST', token, body: { name } });
            setWorkspaces([...workspaces, created]);
            setName('');
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div>
            <h1>Τα workspaces σου</h1>
            {workspaces.length === 0 && (
                <p className="muted">Δεν έχεις κανένα workspace ακόμα. Φτιάξε το πρώτο παρακάτω.</p>
            )}
            <div className="tile-grid">
                {workspaces.map((w) => (
                    <Link key={w.id} to={`/w/${w.id}`} className="tile" data-role={w.role}>
                        <strong>{w.name}</strong>
                        <span className="muted">{w.role}</span>
                    </Link>
                ))}
            </div>

            <form className="inline-form" onSubmit={handleCreate}>
                <input placeholder="Όνομα νέου workspace" value={name}
                       onChange={(e) => setName(e.target.value)} />
                <button type="submit">Δημιουργία</button>
            </form>
            {error && <p className="error">{error}</p>}
        </div>
    );
}