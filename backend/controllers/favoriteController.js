import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import { FavoriteTrip } from '../models/FavoriteTrip.js';
import { Trip } from '../models/Trip.js';

export const listFavorites = asyncHandler(async (req, res) => {
  const favs = await FavoriteTrip.find({ userId: req.user._id })
    .populate('tripId')
    .sort({ createdAt: -1 });
  res.json({ success: true, data: favs });
});

export const addFavorite = asyncHandler(async (req, res) => {
  const { tripId } = req.body;
  const trip = await Trip.findById(tripId);
  if (!trip) throw new AppError('Trip not found', 404);
  try {
    const fav = await FavoriteTrip.create({
      userId: req.user._id,
      tripId,
    });
    const populated = await FavoriteTrip.findById(fav._id).populate('tripId');
    res.status(201).json({ success: true, data: populated });
  } catch (e) {
    if (e.code === 11000) {
      throw new AppError('Already in favorites', 409);
    }
    throw e;
  }
});

export const removeFavorite = asyncHandler(async (req, res) => {
  const r = await FavoriteTrip.findOneAndDelete({
    userId: req.user._id,
    tripId: req.params.tripId,
  });
  if (!r) throw new AppError('Favorite not found', 404);
  res.json({ success: true, message: 'Removed from favorites' });
});
