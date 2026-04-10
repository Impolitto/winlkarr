import mongoose from 'mongoose';

const complaintSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    tripId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Trip',
      default: null,
    },
    type: { type: String, enum: ['passenger', 'driver'], required: true },
    subject: { type: String, required: true, trim: true },
    message: { type: String, required: true, trim: true },
    status: {
      type: String,
      enum: ['pending', 'in_progress', 'resolved'],
      default: 'pending',
    },
    response: { type: String, default: '' },
    priority: {
      type: String,
      enum: ['low', 'medium', 'high'],
      default: 'medium',
    },
    category: {
      type: String,
      enum: ['delay', 'driver', 'bus', 'other'],
      default: 'other',
    },
  },
  { timestamps: { createdAt: 'createdAt', updatedAt: true } }
);

complaintSchema.index({ userId: 1 });
complaintSchema.index({ status: 1 });

export const Complaint = mongoose.model('Complaint', complaintSchema);
