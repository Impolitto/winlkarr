import mongoose from 'mongoose';

const tripRatingSchema = new mongoose.Schema(
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
    score: { type: Number, required: true, min: 1, max: 5 },
    comment: { type: String, default: '', trim: true },
  },
  { timestamps: true }
);

tripRatingSchema.index({ tripId: 1 });
tripRatingSchema.index({ userId: 1, tripId: 1 }, { unique: true });

export const TripRating = mongoose.model('TripRating', tripRatingSchema);
