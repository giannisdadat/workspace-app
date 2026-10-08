import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { api } from './api';

const COLUMNS = [
    { status: 'TODO', label: 'Προς υλοποίηση' },
    { status: 'IN_PROGRESS', label: 'Σε εξέλιξη' },
    { status: 'DONE', label: 'Έγινε' },
];

// Το key κάνει reset όλο το state όταν αλλάζεις workspace
export default function WorkspacePage() {
    const { workspaceId } = useParams();
    return <Workspace key={workspaceId} workspaceId={workspaceId} />;
}

function Workspace({ workspaceId }) {
    const { token, logout } = useAuth();
    const [workspace, setWorkspace] = useState(null);
    const [projects, setProjects] = useState([]);
    const [members, setMembers] = useState([]);
    const [selected, setSelected] = useState(null);
    const [tasks, setTasks] = useState([]);
    const [projectName, setProjectName] = useState('');
    const [taskTitle, setTaskTitle] = useState('');
    const [memberEmail, setMemberEmail] = useState('');
    const [memberRole, setMemberRole] = useState('MEMBER');
    const [error, setError] = useState('');

    const base = `/workspaces/${workspaceId}`;
    const canManage = workspace && workspace.role !== 'MEMBER';
    const selectedProject = projects.find((p) => p.id === selected);

    function handleError(err) {
        if (err.status === 401) logout();
        else setError(err.message);
    }

    useEffect(() => {
        api('/workspaces', { token })
            .then((list) => setWorkspace(list.find((w) => String(w.id) === workspaceId) || null))
            .catch(handleError);
        api(`${base}/projects`, { token })
            .then((list) => {
                setProjects(list);
                setSelected((current) => current ?? list[0]?.id ?? null);
            })
            .catch(handleError);
        api(`${base}/members`, { token }).then(setMembers).catch(handleError);
    }, [workspaceId, token]);

    useEffect(() => {
        if (!selected) return;
        api(`${base}/projects/${selected}/tasks`, { token }).then(setTasks).catch(handleError);
    }, [selected]);

    async function createProject(e) {
        e.preventDefault();
        setError('');
        try {
            const p = await api(`${base}/projects`, { method: 'POST', token, body: { name: projectName } });
            setProjects([...projects, p]);
            setSelected(p.id);
            setTasks([]);
            setProjectName('');
        } catch (err) {
            handleError(err);
        }
    }

    async function createTask(e) {
        e.preventDefault();
        setError('');
        try {
            const t = await api(`${base}/projects/${selected}/tasks`, {
                method: 'POST', token, body: { title: taskTitle },
            });
            setTasks([...tasks, t]);
            setTaskTitle('');
        } catch (err) {
            handleError(err);
        }
    }

    async function changeStatus(task, status) {
        try {
            const updated = await api(`${base}/tasks/${task.id}`, {
                method: 'PATCH', token, body: { status },
            });
            setTasks(tasks.map((t) => (t.id === task.id ? updated : t)));
        } catch (err) {
            handleError(err);
        }
    }

    function move(task, direction) {
        const index = COLUMNS.findIndex((c) => c.status === task.status);
        const target = COLUMNS[index + direction];
        if (target) changeStatus(task, target.status);
    }

    async function deleteTask(task) {
        try {
            await api(`${base}/tasks/${task.id}`, { method: 'DELETE', token });
            setTasks(tasks.filter((t) => t.id !== task.id));
        } catch (err) {
            handleError(err);
        }
    }

    async function addMember(e) {
        e.preventDefault();
        setError('');
        try {
            const m = await api(`${base}/members`, {
                method: 'POST', token, body: { email: memberEmail, role: memberRole },
            });
            setMembers([...members, m]);
            setMemberEmail('');
        } catch (err) {
            if (err.status === 404) setError('Δεν υπάρχει χρήστης με αυτό το email');
            else if (err.status === 409) setError('Είναι ήδη μέλος αυτού του workspace');
            else if (err.status === 403) setError('Δεν έχεις δικαίωμα να προσθέσεις μέλη');
            else handleError(err);
        }
    }

    const memberName = (id) => members.find((m) => m.userId === id)?.name;

    return (
        <div>
            <Link to="/" className="back">← Όλα τα workspaces</Link>
            <h1>{workspace ? workspace.name : 'Workspace'}</h1>
            {workspace && <p className="muted">Ο ρόλος σου: {workspace.role}</p>}
            {error && <p className="error">{error}</p>}

            <div className="workspace-layout">
                <aside>
                    <h2>Projects</h2>
                    <ul className="plain">
                        {projects.map((p) => (
                            <li key={p.id}>
                                <button
                                    className={`project-item${p.id === selected ? ' active' : ''}`}
                                    onClick={() => setSelected(p.id)}
                                >
                                    {p.name}
                                </button>
                            </li>
                        ))}
                    </ul>
                    {projects.length === 0 && <p className="muted">Δεν υπάρχουν projects ακόμα.</p>}
                    {canManage && (
                        <form className="stack" onSubmit={createProject}>
                            <input placeholder="Όνομα νέου project" value={projectName}
                                   onChange={(e) => setProjectName(e.target.value)} />
                            <button type="submit">Δημιουργία project</button>
                        </form>
                    )}

                    <h2>Μέλη</h2>
                    <ul className="plain">
                        {members.map((m) => (
                            <li key={m.userId} className="member">
                                <span>{m.name}</span>
                                <span className="muted">{m.role}</span>
                            </li>
                        ))}
                    </ul>
                    {canManage && (
                        <form className="stack" onSubmit={addMember}>
                            <input type="email" placeholder="Email χρήστη" value={memberEmail}
                                   onChange={(e) => setMemberEmail(e.target.value)} />
                            <select value={memberRole} onChange={(e) => setMemberRole(e.target.value)}>
                                <option value="MEMBER">MEMBER</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>
                            <button type="submit">Προσθήκη μέλους</button>
                        </form>
                    )}
                </aside>

                <section>
                    {selected ? (
                        <>
                            <h2 className="board-title">{selectedProject?.name}</h2>
                            <div className="board">
                                {COLUMNS.map((col) => {
                                    const columnTasks = tasks.filter((t) => t.status === col.status);
                                    return (
                                        <div key={col.status} className="column" data-status={col.status}>
                                            <h3>
                                                {col.label} <span className="count">{columnTasks.length}</span>
                                            </h3>

                                            {columnTasks.map((t) => (
                                                <article key={t.id} className="card">
                                                    <p>{t.title}</p>
                                                    {t.assigneeId && <span className="muted">{memberName(t.assigneeId)}</span>}
                                                    <div className="card-actions">
                                                        <button className="ghost" aria-label="Μετακίνηση προς τα πίσω"
                                                                disabled={col.status === 'TODO'} onClick={() => move(t, -1)}>‹</button>
                                                        <button className="ghost" aria-label="Μετακίνηση προς τα μπροστά"
                                                                disabled={col.status === 'DONE'} onClick={() => move(t, 1)}>›</button>
                                                        {canManage && (
                                                            <button className="danger" onClick={() => deleteTask(t)}>Διαγραφή</button>
                                                        )}
                                                    </div>
                                                </article>
                                            ))}

                                            {col.status === 'TODO' && (
                                                <form className="stack" onSubmit={createTask}>
                                                    <input placeholder="Νέο task" value={taskTitle}
                                                           onChange={(e) => setTaskTitle(e.target.value)} />
                                                    <button type="submit">Προσθήκη task</button>
                                                </form>
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                        </>
                    ) : (
                        <p className="muted">Διάλεξε ένα project αριστερά ή φτιάξε το πρώτο.</p>
                    )}
                </section>
            </div>
        </div>
    );
}