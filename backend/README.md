# Bus Tracking API (backend)

Express + MongoDB (Mongoose) + Socket.IO + JWT. REST is mounted at **`/api`**.

## Requirements

- Node.js 18+
- MongoDB (local or Atlas)

## Setup

1. Copy environment file and edit values:

   ```bash
   cp .env.example .env
   ```

2. Set at least **`MONGODB_URI`** and **`JWT_SECRET`** in `.env`.

3. Install and run:

   ```bash
   npm install
   npm run dev
   ```

   If port **4000** (or your `PORT`) is already in use, change `PORT` in `.env` (e.g. `4001`).

4. Optional seed data (creates admin/driver/passenger and a sample trip):

   ```bash
   npm run seed
   ```

## URLs

| What | URL |
|------|-----|
| Health | `GET http://localhost:<PORT>/api/health` |
| Swagger UI | `http://localhost:<PORT>/api/docs` |
| REST base | `http://localhost:<PORT>/api` |
| Socket.IO | Same host and **port** as HTTP (no `/api` path) |

## Documentation

- **Frontend / mobile clients:** [docs/FRONTEND_INTEGRATION.md](./docs/FRONTEND_INTEGRATION.md) — base URL, JWT, endpoints, Socket.IO events.
- **OpenAPI:** use Swagger UI at `/api/docs`.

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start with `node --watch` |
| `npm start` | Production-style start |
| `npm run seed` | Seed sample users/trip/stations |

## Docker Compose

**Full step-by-step guide (services, ports, env vars, seeding, Redis profile, troubleshooting):**  
→ **[docs/DOCKER_COMPOSE.md](./docs/DOCKER_COMPOSE.md)**

Quick start from **`backend/`**:

```bash
# Set JWT_SECRET in backend/.env (Compose reads it automatically)
docker compose up --build
```

- API: `http://localhost:4000/api` · Swagger: `http://localhost:4000/api/docs`
- Seed inside containers: `docker compose exec api node scripts/seed.js`
- Optional Redis: put `REDIS_URL=redis://redis:6379` in `.env`, then  
  `docker compose --profile cache up --build`

## Project layout

- `config/` — database, Redis, Swagger
- `controllers/`, `routes/`, `services/`, `models/`, `middleware/`
- `sockets/` — Socket.IO (JWT on handshake)
- `docs/` — frontend integration guide + Swagger path definitions
- `examples/` — sample Socket.IO client (`socket-client.mjs`)

## Security notes

- Do not commit `.env`. Keep secrets only in `.env`.
- Use a long random `JWT_SECRET` in production.
