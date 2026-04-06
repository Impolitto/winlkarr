/** Haversine distance in meters between two WGS84 points */
export function haversineMeters(lat1, lng1, lat2, lng2) {
  const R = 6371000;
  const toRad = (d) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

/** Estimate ETA in minutes assuming average speed km/h */
export function etaMinutes(distanceMeters, avgSpeedKmh = 25) {
  if (!distanceMeters || distanceMeters <= 0) return 0;
  const hours = distanceMeters / 1000 / avgSpeedKmh;
  return Math.max(0, Math.round(hours * 60));
}
