import { Trip } from '../models/Trip.js';
import { redisGet, redisSet } from '../config/redis.js';
import { haversineMeters } from '../utils/geo.js';

function toLatLng(doc) {
  if (!doc?.currentLocation?.coordinates?.length) return null;
  const [lng, lat] = doc.currentLocation.coordinates;
  return { lat, lng };
}

export function setLocationFields(tripDoc, lat, lng) {
  tripDoc.currentLocation = {
    type: 'Point',
    coordinates: [lng, lat],
  };
}

/**
 * Active trips within maxDistanceMeters of [lng, lat], sorted by distance.
 */
export async function findNearestActiveTrips(lng, lat, maxDistanceMeters = 5000) {
  const cacheKey = `nearest:${lat.toFixed(4)}:${lng.toFixed(4)}`;
  const cached = await redisGet(cacheKey);
  if (cached) {
    try {
      return JSON.parse(cached);
    } catch {
      /* fall through */
    }
  }

  const trips = await Trip.find({
    status: 'active',
    currentLocation: {
      $near: {
        $geometry: { type: 'Point', coordinates: [lng, lat] },
        $maxDistance: maxDistanceMeters,
      },
    },
  })
    .populate('busId', 'busNumber capacity')
    .populate('driverId', 'name email')
    .lean();

  const withDistance = trips.map((t) => {
    const pos = toLatLng(t);
    const distanceMeters = pos
      ? haversineMeters(lat, lng, pos.lat, pos.lng)
      : null;
    return {
      ...t,
      distanceMeters,
      currentLocationLatLng: pos,
    };
  });

  withDistance.sort(
    (a, b) => (a.distanceMeters ?? Infinity) - (b.distanceMeters ?? Infinity)
  );

  await redisSet(cacheKey, JSON.stringify(withDistance), 30);
  return withDistance;
}
