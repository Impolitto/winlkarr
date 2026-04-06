import mongoose from 'mongoose';

const stationSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true },
    lat: { type: Number, required: true },
    lng: { type: Number, required: true },
    order: { type: Number, required: true, min: 0 },
    tripId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Trip',
      required: true,
    },
  },
  { timestamps: true }
);

stationSchema.index({ tripId: 1, order: 1 });

export const Station = mongoose.model('Station', stationSchema);
