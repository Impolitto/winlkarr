import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import { Trip } from '../models/Trip.js';
import { setLocationFields, findNearestActiveTrips } from '../services/tripGeoService.js';
import { computeStationEtas } from '../services/etaService.js';
import { getIo } from '../sockets/index.js';
import { normalizeTripPayload } from '../utils/normalizeTrip.js';

function assertDriver(trip, userId) {
  if (!trip.driverId || trip.driverId.toString() !== userId.toString()) {
    throw new AppError('You are not assigned to this trip', 403);
  }
}

function tripResponse(doc) {
  const o = doc.toObject ? doc.toObject() : { ...doc };
  if (o.currentLocation?.coordinates?.length === 2) {
    const [lng, lat] = o.currentLocation.coordinates;
    o.currentLocationLatLng = { lat, lng };
  }
  return o;
}

export const listTrips = asyncHandler(async (req, res) => {
  const trips = await Trip.find()
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email')
    .sort({ createdAt: -1 });
  res.json({ success: true, data: trips.map(tripResponse) });
});

export const getTrip = asyncHandler(async (req, res) => {
  const trip = await Trip.findById(req.params.id)
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email');
  if (!trip) throw new AppError('Trip not found', 404);
  res.json({ success: true, data: tripResponse(trip) });
});

export const createTrip = asyncHandler(async (req, res) => {
  const trip = await Trip.create(normalizeTripPayload(req.body));
  res.status(201).json({ success: true, data: tripResponse(trip) });
});

export const updateTrip = asyncHandler(async (req, res) => {
  const trip = await Trip.findByIdAndUpdate(
    req.params.id,
    normalizeTripPayload(req.body),
    {
      new: true,
      runValidators: true,
    }
  );
  if (!trip) throw new AppError('Trip not found', 404);
  res.json({ success: true, data: tripResponse(trip) });
});

export const deleteTrip = asyncHandler(async (req, res) => {
  const trip = await Trip.findByIdAndDelete(req.params.id);
  if (!trip) throw new AppError('Trip not found', 404);
  res.json({ success: true, message: 'Trip deleted' });
});

export const startTrip = asyncHandler(async (req, res) => {
  const trip = await Trip.findById(req.params.id);
  if (!trip) throw new AppError('Trip not found', 404);
  assertDriver(trip, req.user._id);
  if (trip.status !== 'pending') {
    throw new AppError('Trip cannot be started in current state', 400);
  }
  trip.status = 'active';
  trip.startTime = new Date();
  await trip.save();
  const populated = await Trip.findById(trip._id)
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email');
  const payload = tripResponse(populated);
  getIo()?.to(`trip:${trip._id}`).emit('trip:started', payload);
  res.json({ success: true, data: payload });
});

export const updateTripLocation = asyncHandler(async (req, res) => {
  const { lat, lng, currentStation, nextStation } = req.body;
  if (typeof lat !== 'number' || typeof lng !== 'number') {
    throw new AppError('lat and lng are required numbers', 400);
  }
  const trip = await Trip.findById(req.params.id);
  if (!trip) throw new AppError('Trip not found', 404);
  assertDriver(trip, req.user._id);
  if (trip.status !== 'active') {
    throw new AppError('Trip is not active', 400);
  }
  setLocationFields(trip, lat, lng);
  if (currentStation !== undefined) trip.currentStation = currentStation;
  if (nextStation !== undefined) trip.nextStation = nextStation;
  await trip.save();
  const populated = await Trip.findById(trip._id)
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email');
  const payload = tripResponse(populated);
  getIo()?.to(`trip:${trip._id}`).emit('trip:location', payload);
  res.json({ success: true, data: payload });
});

export const updateTripStatusDriver = asyncHandler(async (req, res) => {
  const { status } = req.body;
  const trip = await Trip.findById(req.params.id);
  if (!trip) throw new AppError('Trip not found', 404);
  assertDriver(trip, req.user._id);
  if (!['pending', 'active', 'completed'].includes(status)) {
    throw new AppError('Invalid status', 400);
  }
  trip.status = status;
  await trip.save();
  const populated = await Trip.findById(trip._id)
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email');
  const payload = tripResponse(populated);
  getIo()?.to(`trip:${trip._id}`).emit('trip:status', payload);
  res.json({ success: true, data: payload });
});

export const endTrip = asyncHandler(async (req, res) => {
  const trip = await Trip.findById(req.params.id);
  if (!trip) throw new AppError('Trip not found', 404);
  assertDriver(trip, req.user._id);
  if (trip.status !== 'active') {
    throw new AppError('Trip is not active', 400);
  }
  trip.status = 'completed';
  trip.endTime = new Date();
  await trip.save();
  const populated = await Trip.findById(trip._id)
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email');
  const payload = tripResponse(populated);
  getIo()?.to(`trip:${trip._id}`).emit('trip:ended', payload);
  res.json({ success: true, data: payload });
});

export const listActiveTrips = asyncHandler(async (req, res) => {
  const trips = await Trip.find({ status: 'active' })
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email')
    .sort({ startTime: -1 });
  res.json({ success: true, data: trips.map(tripResponse) });
});

export const nearestBuses = asyncHandler(async (req, res) => {
  const lat = Number(req.query.lat);
  const lng = Number(req.query.lng);
  const maxDistance = Number(req.query.maxDistance) || 5000;
  if (Number.isNaN(lat) || Number.isNaN(lng)) {
    throw new AppError('Query params lat and lng are required', 400);
  }
  const data = await findNearestActiveTrips(lng, lat, maxDistance);
  res.json({ success: true, data });
});

export const tripStationEtas = asyncHandler(async (req, res) => {
  const trip = await Trip.findById(req.params.id);
  if (!trip) throw new AppError('Trip not found', 404);
  const coords = trip.currentLocation?.coordinates;
  if (!coords?.length) {
    throw new AppError('Trip has no current GPS position yet', 400);
  }
  const [lng, lat] = coords;
  const avgSpeed = Number(req.query.avgSpeedKmh) || 25;
  const etas = await computeStationEtas(trip._id, lat, lng, avgSpeed);
  res.json({ success: true, data: etas });
});
