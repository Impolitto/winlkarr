import { Station } from '../models/Station.js';
import { haversineMeters, etaMinutes } from '../utils/geo.js';

/**
 * Per-station ETAs: direct line from bus to each stop, plus cumulative along ordered stops.
 */
export async function computeStationEtas(tripId, busLat, busLng, avgSpeedKmh = 25) {
  const stations = await Station.find({ tripId })
    .sort({ order: 1 })
    .lean();

  let cursorLat = busLat;
  let cursorLng = busLng;
  let cumulativeMeters = 0;

  return stations.map((s) => {
    const segmentMeters = haversineMeters(cursorLat, cursorLng, s.lat, s.lng);
    cumulativeMeters += segmentMeters;
    const directMeters = haversineMeters(busLat, busLng, s.lat, s.lng);

    cursorLat = s.lat;
    cursorLng = s.lng;

    return {
      stationId: s._id,
      name: s.name,
      order: s.order,
      directDistanceMeters: Math.round(directMeters),
      directEtaMinutes: etaMinutes(directMeters, avgSpeedKmh),
      cumulativeAlongRouteMeters: Math.round(cumulativeMeters),
      cumulativeAlongRouteEtaMinutes: etaMinutes(cumulativeMeters, avgSpeedKmh),
    };
  });
}
