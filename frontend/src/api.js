const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

export async function api(path, { method = 'GET', body, token } = {}) {
    const res = await fetch(BASE + path, {
        method,
        headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: body ? JSON.stringify(body) : undefined,
    });

    if (!res.ok) {
        let message = `Error ${res.status}`;
        try {
            const data = await res.json();
            if (data.message) message = data.message;
        } catch { /* δεν υπήρχε JSON */ }
        const err = new Error(message);
        err.status = res.status;
        throw err;
    }

    return res.status === 204 ? null : res.json();
}