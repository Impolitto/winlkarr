import { Complaint } from '../models/Complaint.js';
import { createNotification } from './notificationService.js';

export async function createComplaint(data) {
  return Complaint.create(data);
}

export async function listByUser(userId) {
  return Complaint.find({ userId })
    .sort({ createdAt: -1 })
    .populate('tripId', 'name from to status');
}

export async function listAll(filters = {}) {
  const q = {};
  if (filters.status) q.status = filters.status;
  return Complaint.find(q)
    .sort({ createdAt: -1 })
    .populate('userId', 'name email role')
    .populate('tripId', 'name from to');
}

export async function updateComplaintAdmin(id, { status, response }) {
  const complaint = await Complaint.findById(id);
  if (!complaint) return null;

  const prevStatus = complaint.status;
  if (status) complaint.status = status;
  if (response !== undefined) complaint.response = response;
  await complaint.save();

  if (status === 'resolved' && prevStatus !== 'resolved') {
    await createNotification({
      userId: complaint.userId,
      title: 'Complaint resolved',
      message:
        complaint.response ||
        'Your complaint has been marked as resolved. Thank you for your feedback.',
      type: 'complaint_resolved',
      meta: { complaintId: complaint._id.toString() },
    });
  }

  return complaint;
}
