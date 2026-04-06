import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import { TripHistory } from '../models/TripHistory.js';
import { Trip } from '../models/Trip.js';

export const listHistory = asyncHandler(async (req, res) => {
  const items = await TripHistory.find({ userId: req.user._id })
    .populate('tripId')
    .sort({ recordedAt: -1 });
  res.json({ success: true, data: items });
});

export const addHistory = asyncHandler(async (req, res) => {
  const { tripId, note } = req.body;
  const trip = await Trip.findById(tripId);
  if (!trip) throw new AppError('Trip not found', 404);
  const item = await TripHistory.create({
    userId: req.user._id,
    tripId,
    note: note || '',
  });
  const populated = await TripHistory.findById(item._id).populate('tripId');
  res.status(201).json({ success: true, data: populated });
});

export const removeHistory = asyncHandler(async (req, res) => {
  const r = await TripHistory.findOneAndDelete({
    _id: req.params.id,
    userId: req.user._id,
  });
  if (!r) throw new AppError('History entry not found', 404);
  res.json({ success: true, message: 'Removed' });
});
