import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { api } from './api';

export default function RegisterPage() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');

        if (password.length < 8) {
            setError('Ο κωδικός πρέπει να έχει τουλάχιστον 8 χαρακτήρες');
            return;
        }

        try {
            await api('/auth/register', { method: 'POST', body: { name, email, password } });
            await login(email, password);
            navigate('/');
        } catch (err) {
            if (err.status === 409) setError('Το email χρησιμοποιείται ήδη');
            else if (err.status === 400) setError('Έλεγξε τα στοιχεία (σωστό email και όνομα)');
            else setError(err.message);
        }
    }

    return (
        <div className="auth">
            <div className="auth-card">
                <h1>Δημιουργία λογαριασμού</h1>
                <form className="stack" onSubmit={handleSubmit}>
                    <input placeholder="Όνομα" value={name}
                           onChange={(e) => setName(e.target.value)} />
                    <input type="email" placeholder="Email" value={email}
                           onChange={(e) => setEmail(e.target.value)} />
                    <input type="password" placeholder="Κωδικός (τουλάχιστον 8 χαρακτήρες)" value={password}
                           onChange={(e) => setPassword(e.target.value)} />
                    <button type="submit">Δημιουργία λογαριασμού</button>
                </form>
                {error && <p className="error">{error}</p>}
                <p className="muted">Έχεις ήδη λογαριασμό; <Link to="/login">Σύνδεση</Link></p>
            </div>
        </div>

    );
}