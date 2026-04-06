import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import { Bus } from '../models/Bus.js';

export const listBuses = asyncHandler(async (req, res) => {
  const buses = await Bus.find().sort({ busNumber: 1 });
  res.json({ success: true, data: buses });
});

export const getBus = asyncHandler(async (req, res) => {
  const bus = await Bus.findById(req.params.id);
  if (!bus) throw new AppError('Bus not found', 404);
  res.json({ success: true, data: bus });
});

export const createBus = asyncHandler(async (req, res) => {
  const bus = await Bus.create(req.body);
  res.status(201).json({ success: true, data: bus });
});

export const updateBus = asyncHandler(async (req, res) => {
  const bus = await Bus.findByIdAndUpdate(req.params.id, req.body, {
    new: true,
    runValidators: true,
  });
  if (!bus) throw new AppError('Bus not found', 404);
  res.json({ success: true, data: bus });
});

export const deleteBus = asyncHandler(async (req, res) => {
  const bus = await Bus.findByIdAndDelete(req.params.id);
  if (!bus) throw new AppError('Bus not found', 404);
  res.json({ success: true, message: 'Bus deleted' });
});
