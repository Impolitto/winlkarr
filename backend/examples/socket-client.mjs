/**
 * Sample Socket.IO client (ESM). Run from backend folder after `npm install socket.io-client`:
 *   npm install socket.io-client
 *   node examples/socket-client.mjs <JWT_TOKEN> <TRIP_ID>
 *
 * Or pass TOKEN and TRIP_ID via env:
 *   set TOKEN=...&& set TRIP_ID=...&& node examples/socket-client.mjs
 */
import { io } from 'socket.io-client';

const token = process.argv[2] || process.env.TOKEN;
const tripId = process.argv[3] || process.env.TRIP_ID;
const url = process.env.API_URL || 'http://localhost:4000';

if (!token || !tripId) {
  console.error('Usage: node examples/socket-client.mjs <JWT_TOKEN> <TRIP_ID>');
  process.exit(1);
}

const socket = io(url, {
  auth: { token },
  transports: ['websocket'],
});

socket.on('connect', () => {
  console.log('connected', socket.id);
  socket.emit('trip:join', { tripId });
});

socket.on('connected', (p) => console.log('server ack:', p));
socket.on('trip:started', (p) => console.log('trip:started', p?.name || p));
socket.on('trip:location', (p) =>
  console.log('trip:location', p?.currentLocationLatLng || p?.currentLocation)
);
socket.on('trip:status', (p) => console.log('trip:status', p?.status));
socket.on('trip:ended', (p) => console.log('trip:ended', p?.name || p));
socket.on('disconnect', (r) => console.log('disconnect', r));
socket.on('connect_error', (e) => console.error('connect_error', e.message));
