export function normalizeTripPayload(body) {
  if (!body || typeof body !== 'object') return body;
  const b = { ...body };
  if (
    b.currentLocation &&
    typeof b.currentLocation.lat === 'number' &&
    typeof b.currentLocation.lng === 'number'
  ) {
    b.currentLocation = {
      type: 'Point',
      coordinates: [b.currentLocation.lng, b.currentLocation.lat],
    };
  }
  return b;
}
