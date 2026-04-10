import mongoose from 'mongoose';

const favoriteTripSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    tripId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Trip',
      required: true,
    },
  },
  { timestamps: true }
);

favoriteTripSchema.index({ userId: 1, tripId: 1 }, { unique: true });

export const FavoriteTrip = mongoose.model('FavoriteTrip', favoriteTripSchema);
