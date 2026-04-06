import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import * as notificationService from '../services/notificationService.js';

export const listNotifications = asyncHandler(async (req, res) => {
  const read =
    req.query.read === 'true' ? true : req.query.read === 'false' ? false : undefined;
  const data = await notificationService.listForUser(req.user._id, { read });
  res.json({ success: true, data });
});

export const markOneRead = asyncHandler(async (req, res) => {
  const n = await notificationService.markRead(req.user._id, req.params.id);
  if (!n) throw new AppError('Notification not found', 404);
  res.json({ success: true, data: n });
});

export const markAllRead = asyncHandler(async (req, res) => {
  await notificationService.markAllRead(req.user._id);
  res.json({ success: true, message: 'All marked read' });
});
