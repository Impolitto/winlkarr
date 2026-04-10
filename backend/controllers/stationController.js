import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import { Station } from '../models/Station.js';

export const listByTrip = asyncHandler(async (req, res) => {
  const { tripId } = req.params;
  const stations = await Station.find({ tripId }).sort({ order: 1 });
  res.json({ success: true, data: stations });
});

export const createStation = asyncHandler(async (req, res) => {
  const station = await Station.create(req.body);
  res.status(201).json({ success: true, data: station });
});

export const updateStation = asyncHandler(async (req, res) => {
  const station = await Station.findByIdAndUpdate(req.params.id, req.body, {
    new: true,
    runValidators: true,
  });
  if (!station) throw new AppError('Station not found', 404);
  res.json({ success: true, data: station });
});

export const deleteStation = asyncHandler(async (req, res) => {
  const station = await Station.findByIdAndDelete(req.params.id);
  if (!station) throw new AppError('Station not found', 404);
  res.json({ success: true, message: 'Station deleted' });
});
