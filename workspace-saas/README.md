# Workspace

A multi-tenant collaborative task board. Users create workspaces, invite members with roles, and manage projects and tasks on a status board. Data is isolated per workspace.

## Features

- Register and login with JWT authentication
- A user can belong to many workspaces, with a role in each (OWNER, ADMIN, MEMBER)
- Role-based permissions enforced on the backend
- Projects and tasks per workspace, shown as a board (To do, In progress, Done)
- Add members to a workspace by email

## Tech stack

- **Backend:** Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway
- **Database:** PostgreSQL 16
- **Frontend:** React, Vite, React Router

## Multi-tenancy

Shared database with a `workspace_id` column on tenant-owned tables. Isolation has two layers:

1. Every service method first checks that the user is a member of the workspace (404 if not, 403 if the role is too low), and repositories query by `id` and `workspace_id` together.
2. A Hibernate filter adds `workspace_id = ?` to every `Project` and `Task` query as a safety net.

## Run locally

Requirements: Java 21, Node 20+, Docker.

```bash
# 1. Database
docker run --name workspace-db -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=workspace_saas -p 5432:5432 -d postgres:16

# 2. Backend (http://localhost:8080)
cd workspace-saas
./mvnw spring-boot:run

# 3. Frontend (http://localhost:5173)
cd frontend
npm install
npm run dev
```

Configuration is read from environment variables, with local defaults: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`. Set your own values in any deployed environment.

## API overview

| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Public |
| GET, POST | `/api/workspaces` | List and create |
| GET, POST | `/api/workspaces/{id}/members` | Add requires ADMIN or OWNER |
| GET, POST | `/api/workspaces/{id}/projects` | Create requires ADMIN or OWNER |
| GET, POST | `/api/workspaces/{id}/projects/{pid}/tasks` | Any member |
| PATCH, DELETE | `/api/workspaces/{id}/tasks/{tid}` | Delete requires ADMIN or OWNER |