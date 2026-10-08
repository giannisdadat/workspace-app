import { useState } from 'react';

import { useAuth } from './AuthContext';
import { Link, useNavigate } from 'react-router-dom';

export default function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        try {
            await login(email, password);
            navigate('/');
        } catch (err) {
            setError(err.status === 401 ? 'Λάθος email ή κωδικός' : err.message);
        }
    }

    return (
        <div className="auth">
            <div className="auth-card">
                <h1>Σύνδεση</h1>
                <form className="stack" onSubmit={handleSubmit}>
                    <input type="email" placeholder="Email" value={email}
                           onChange={(e) => setEmail(e.target.value)} />
                    <input type="password" placeholder="Κωδικός" value={password}
                           onChange={(e) => setPassword(e.target.value)} />
                    <button type="submit">Σύνδεση</button>
                </form>
                {error && <p className="error">{error}</p>}
                <p className="muted">Δεν έχεις λογαριασμό; <Link to="/register">Δημιούργησε έναν</Link></p>
            </div>
        </div>
    );
}