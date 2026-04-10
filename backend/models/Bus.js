import mongoose from 'mongoose';

const busSchema = new mongoose.Schema(
  {
    busNumber: { type: String, required: true, unique: true, trim: true },
    capacity: { type: Number, required: true, min: 1 },
  },
  { timestamps: true }
);

export const Bus = mongoose.model('Bus', busSchema);
