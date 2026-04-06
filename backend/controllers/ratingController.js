import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import { TripRating } from '../models/TripRating.js';
import { Trip } from '../models/Trip.js';

export const rateTrip = asyncHandler(async (req, res) => {
  const { tripId, score, comment } = req.body;
  const trip = await Trip.findById(tripId);
  if (!trip) throw new AppError('Trip not found', 404);
  try {
    const rating = await TripRating.create({
      userId: req.user._id,
      tripId,
      score,
      comment: comment || '',
    });
    res.status(201).json({ success: true, data: rating });
  } catch (e) {
    if (e.code === 11000) {
      throw new AppError('You already rated this trip', 409);
    }
    throw e;
  }
});

export const listTripRatings = asyncHandler(async (req, res) => {
  const ratings = await TripRating.find({ tripId: req.params.tripId })
    .populate('userId', 'name')
    .sort({ createdAt: -1 });
  const avg =
    ratings.length === 0
      ? null
      : ratings.reduce((s, r) => s + r.score, 0) / ratings.length;
  res.json({
    success: true,
    data: { average: avg ? Math.round(avg * 10) / 10 : null, ratings },
  });
});

export const myRatings = asyncHandler(async (req, res) => {
  const ratings = await TripRating.find({ userId: req.user._id })
    .populate('tripId', 'name from to')
    .sort({ createdAt: -1 });
  res.json({ success: true, data: ratings });
});
