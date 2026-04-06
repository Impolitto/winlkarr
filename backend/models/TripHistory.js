import mongoose from 'mongoose';

const tripHistorySchema = new mongoose.Schema(
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
    note: { type: String, default: '' },
  },
  { timestamps: { createdAt: 'recordedAt', updatedAt: false } }
);

tripHistorySchema.index({ userId: 1, recordedAt: -1 });

export const TripHistory = mongoose.model('TripHistory', tripHistorySchema);
