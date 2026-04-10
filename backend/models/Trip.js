import mongoose from 'mongoose';

const tripSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true },
    from: { type: String, required: true, trim: true },
    to: { type: String, required: true, trim: true },
    status: {
      type: String,
      enum: ['pending', 'active', 'completed'],
      default: 'pending',
    },
    driverId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      default: null,
    },
    busId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Bus',
      required: true,
    },
    /** GeoJSON Point for 2dsphere queries — coordinates: [lng, lat] */
    currentLocation: {
      type: {
        type: String,
        enum: ['Point'],
        default: 'Point',
      },
      coordinates: {
        type: [Number],
        default: undefined,
      },
    },
    currentStation: { type: String, default: '' },
    nextStation: { type: String, default: '' },
    startTime: { type: Date, default: null },
    endTime: { type: Date, default: null },
  },
  { timestamps: true }
);

tripSchema.index({ currentLocation: '2dsphere' });
tripSchema.index({ status: 1 });
tripSchema.index({ driverId: 1 });

export const Trip = mongoose.model('Trip', tripSchema);
