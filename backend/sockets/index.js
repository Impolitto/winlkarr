import { Server } from 'socket.io';
import { verifyToken } from '../utils/jwt.js';
import { User } from '../models/User.js';

let ioInstance = null;

export function getIo() {
  return ioInstance;
}

export function initSocket(httpServer) {
  const corsOrigin = process.env.SOCKET_CORS_ORIGIN || '*';
  const origins =
    corsOrigin === '*'
      ? '*'
      : corsOrigin.split(',').map((s) => s.trim());

  const io = new Server(httpServer, {
    cors: {
      origin: origins,
      methods: ['GET', 'POST'],
    },
  });

  io.use(async (socket, next) => {
    try {
      const token =
        socket.handshake.auth?.token ||
        socket.handshake.query?.token ||
        socket.handshake.headers?.authorization?.replace('Bearer ', '');
      if (!token) {
        return next(new Error('Authentication required'));
      }
      const decoded = verifyToken(token);
      const user = await User.findById(decoded.sub).select('-password');
      if (!user) return next(new Error('User not found'));
      socket.data.user = user;
      next();
    } catch {
      next(new Error('Invalid token'));
    }
  });

  io.on('connection', (socket) => {
    socket.emit('connected', { userId: socket.data.user._id.toString() });

    socket.on('trip:join', ({ tripId }) => {
      if (!tripId) return;
      socket.join(`trip:${tripId}`);
    });

    socket.on('trip:leave', ({ tripId }) => {
      if (!tripId) return;
      socket.leave(`trip:${tripId}`);
    });
  });

  ioInstance = io;
  return io;
}
