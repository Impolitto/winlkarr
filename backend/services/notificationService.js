import { Notification } from '../models/Notification.js';

export async function createNotification({
  userId,
  title,
  message,
  type = 'other',
  meta = {},
}) {
  return Notification.create({ userId, title, message, type, meta });
}

export async function listForUser(userId, { read, limit = 50 } = {}) {
  const q = { userId };
  if (read === true || read === false) q.read = read;
  return Notification.find(q).sort({ createdAt: -1 }).limit(limit).lean();
}

export async function markRead(userId, id) {
  const n = await Notification.findOneAndUpdate(
    { _id: id, userId },
    { read: true },
    { new: true }
  );
  return n;
}

export async function markAllRead(userId) {
  await Notification.updateMany({ userId, read: false }, { read: true });
}
