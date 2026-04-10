import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import * as complaintService from '../services/complaintService.js';

export const createComplaint = asyncHandler(async (req, res) => {
  if (!['passenger', 'driver'].includes(req.user.role)) {
    throw new AppError('Only passengers and drivers can file complaints', 403);
  }
  const type = req.user.role === 'driver' ? 'driver' : 'passenger';
  const complaint = await complaintService.createComplaint({
    userId: req.user._id,
    tripId: req.body.tripId || null,
    type,
    subject: req.body.subject,
    message: req.body.message,
    priority: req.body.priority,
    category: req.body.category,
  });
  res.status(201).json({ success: true, data: complaint });
});

export const myComplaints = asyncHandler(async (req, res) => {
  const data = await complaintService.listByUser(req.user._id);
  res.json({ success: true, data });
});

export const listAllComplaints = asyncHandler(async (req, res) => {
  const data = await complaintService.listAll({
    status: req.query.status,
  });
  res.json({ success: true, data });
});

export const updateComplaint = asyncHandler(async (req, res) => {
  const complaint = await complaintService.updateComplaintAdmin(
    req.params.id,
    req.body
  );
  if (!complaint) throw new AppError('Complaint not found', 404);
  res.json({ success: true, data: complaint });
});
