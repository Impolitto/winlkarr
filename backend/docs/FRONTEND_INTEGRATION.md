# Frontend integration guide (Bus Tracking API)

This document helps mobile or web clients connect to the Node.js backend. All REST routes are under **`/api`**. Replace host and port with your environment.

- **Docker Compose** publishes the API on **`4000`** by default (see [DOCKER_COMPOSE.md](./DOCKER_COMPOSE.md)).
- **Local `npm run dev`** uses whatever **`PORT`** you set in `backend/.env` (e.g. `4001` if `4000` is busy).

## Base URL

| Environment | Example base URL (Docker: port **4000**) |
|------------|------------------------------------------|
| Android emulator → host machine | `http://10.0.2.2:4000/api` |
| iOS simulator / same machine    | `http://localhost:4000/api` |
| Physical device (same Wi‑Fi)  | `http://<your-pc-lan-ip>:4000/api` |

Socket.IO uses the **same host and port** as HTTP **without** `/api` (e.g. `http://10.0.2.2:4000`).

Interactive docs: **`GET /api/docs`** (Swagger UI), e.g. `http://localhost:4000/api/docs`.

---

## Authentication (JWT)

1. **Register** or **login** → response includes `data.token` (JWT) and `data.user`.
2. Store the token securely (e.g. EncryptedSharedPreferences / Keychain / `httpOnly` cookie if you add a web BFF).
3. Send on every protected request:

```http
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

4. **Current user:** `GET /api/auth/me` with the header above.

**Token payload** includes the user id (`sub` in JWT) and `role`. The backend checks **role** for admin/driver/passenger routes.

### Auth endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | No | Body: `name`, `email`, `password`, optional `role` (`passenger` \| `driver`). Cannot self-register as `admin`. |
| POST | `/api/auth/login` | No | Body: `email`, `password`. |
| GET | `/api/auth/me` | Yes | Returns current user (no password). |

**Login / register success shape:**

```json
{
  "success": true,
  "data": {
    "user": { "_id", "name", "email", "role", "createdAt" },
    "token": "eyJ..."
  }
}
```

---

## Roles and what the UI should show

| Role | Typical UI |
|------|------------|
| `passenger` | Active trips, nearest buses, stations, favorites, history, ratings, complaints, notifications |
| `driver` | Assigned trips: start trip, send GPS, end trip; complaints |
| `admin` | User/bus/trip/station CRUD, all complaints, full management |

If the UI calls an endpoint the role is not allowed to use, the API returns **403** with a JSON `message`.

---

## REST endpoints (summary)

Unless noted, **Bearer token is required**.

### Users

| Method | Path | Who |
|--------|------|-----|
| PATCH | `/api/users/me` | Any |
| DELETE | `/api/users/me` | Any |
| GET, POST | `/api/users` | Admin |
| GET, PATCH, DELETE | `/api/users/:id` | Admin |

### Buses (admin)

| Method | Path |
|--------|------|
| GET | `/api/buses` |
| POST | `/api/buses` |
| GET, PATCH, DELETE | `/api/buses/:id` |

### Trips

| Method | Path | Who / notes |
|--------|------|-------------|
| GET | `/api/trips/active` | Active trips (for passengers / maps) |
| GET | `/api/trips/nearest?lat=&lng=&maxDistance=` | Query: `lat`, `lng` required; `maxDistance` in meters (default 5000) |
| GET | `/api/trips/:id/station-etas` | Optional query `avgSpeedKmh` (default 25) |
| PATCH | `/api/trips/:id/start` | Driver assigned to trip |
| PATCH | `/api/trips/:id/location` | Driver; body: `lat`, `lng`, optional `currentStation`, `nextStation` |
| PATCH | `/api/trips/:id/status` | Driver; body: `status` (`pending` \| `active` \| `completed`) |
| PATCH | `/api/trips/:id/end` | Driver; completes trip |
| GET, POST | `/api/trips` | Admin |
| GET, PATCH, DELETE | `/api/trips/:id` | GET: any authenticated user; PATCH/DELETE: admin |

**Trip location in JSON:** responses may include `currentLocation` (GeoJSON) and a convenience field `currentLocationLatLng: { lat, lng }` when a position exists.

Admin **create/update** trip may send `currentLocation` as `{ "lat": number, "lng": number }`; the server converts it to GeoJSON.

### Stations

| Method | Path | Who |
|--------|------|-----|
| GET | `/api/stations/trip/:tripId` | Any authenticated |
| POST | `/api/stations` | Admin |
| PATCH, DELETE | `/api/stations/:id` | Admin |

Body for create: `name`, `lat`, `lng`, `order`, `tripId`.

### Complaints

| Method | Path | Who |
|--------|------|-----|
| POST | `/api/complaints` | Passenger / driver |
| GET | `/api/complaints/me` | Own list |
| GET | `/api/complaints` | Admin (optional `?status=`) |
| PATCH | `/api/complaints/:id` | Admin (`status`, `response`) |

When a complaint is set to **resolved**, the user gets a **notification** (`type`: `complaint_resolved`).

### Notifications

| Method | Path |
|--------|------|
| GET | `/api/notifications` optional `?read=true` or `false` |
| PATCH | `/api/notifications/:id/read` |
| POST | `/api/notifications/read-all` |

### Favorites (passenger)

| Method | Path |
|--------|------|
| GET | `/api/favorites` |
| POST | `/api/favorites` body: `{ "tripId" }` |
| DELETE | `/api/favorites/:tripId` |

### Trip history (passenger)

| Method | Path |
|--------|------|
| GET | `/api/history` |
| POST | `/api/history` body: `{ "tripId", "note?" }` |
| DELETE | `/api/history/:id` |

### Ratings

| Method | Path | Who |
|--------|------|-----|
| POST | `/api/ratings` | Passenger; body: `tripId`, `score` (1–5), optional `comment` |
| GET | `/api/ratings/me` | Passenger |
| GET | `/api/ratings/trip/:tripId` | Any authenticated |

### Health

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/health` | No |

---

## Error and success shapes

**Success (typical):**

```json
{ "success": true, "data": { ... } }
```

**Error:**

```json
{ "success": false, "message": "Human readable message" }
```

HTTP status: **401** (no/invalid token), **403** (wrong role), **404**, **409** (duplicate), **400** (validation), **429** (rate limit on auth routes).

---

## Socket.IO (live trip updates)

Use a Socket.IO **client** library; connect to the server root (no `/api` path).

### Connection

- **Auth:** pass the JWT in one of:
  - `auth: { token: "<jwt>" }`, or
  - query `?token=<jwt>`, or
  - header `Authorization: Bearer <jwt>` (if your client supports it on the handshake).

### Client → server

| Event | Payload | Purpose |
|-------|---------|---------|
| `trip:join` | `{ tripId: string }` | Subscribe to room for that trip |
| `trip:leave` | `{ tripId: string }` | Unsubscribe |

### Server → client (same room)

Emitted when the driver updates the trip via REST (or when trip starts/ends):

| Event | When |
|-------|------|
| `trip:started` | Driver started trip |
| `trip:location` | Driver sent GPS update |
| `trip:status` | Driver changed status |
| `trip:ended` | Driver ended trip |

Payload is trip JSON (includes `currentLocationLatLng` when available).

**Flow for passengers:** after login, connect socket → `trip:join` with the trip id they are watching → listen for `trip:location` (and others) to move a map marker.

A minimal Node sample ships at `backend/examples/socket-client.mjs`.

---

## CORS

Backend uses `CORS_ORIGIN` from `.env` (`*` by default). For cookie-based auth later, you would restrict origins and enable credentials in both client and server.

---

## Rate limiting

Auth routes (`/api/auth/register`, `/api/auth/login`) have stricter limits than the rest of the API. If the UI hits **429**, show a “try again later” message.

---

## Quick checklist for frontend devs

1. [ ] Configure **base URL** per platform (emulator vs device).
2. [ ] Persist **JWT** after login; attach **Authorization** header to API calls.
3. [ ] Branch UI by **`user.role`**.
4. [ ] For live map: **Socket.IO** + `trip:join` + `trip:location`.
5. [ ] Use **Swagger** at `/api/docs` to try requests and copy request shapes.
6. [ ] If the API runs in **Docker**, default host port is **4000** — see [DOCKER_COMPOSE.md](./DOCKER_COMPOSE.md).

If something is missing or unclear, extend this file next to the feature you add on the backend.
