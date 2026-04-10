import Redis from 'ioredis';

let client = null;

/**
 * Returns a Redis client if REDIS_URL is set; otherwise null.
 */
export function getRedis() {
  const url = process.env.REDIS_URL;
  if (!url) return null;
  if (!client) {
    client = new Redis(url, {
      maxRetriesPerRequest: 3,
      lazyConnect: true,
    });
    client.on('error', (err) => {
      console.error('Redis error:', err.message);
    });
  }
  return client;
}

export async function redisGet(key) {
  const r = getRedis();
  if (!r) return null;
  try {
    if (r.status !== 'ready' && r.status !== 'connecting') await r.connect();
    return await r.get(key);
  } catch {
    return null;
  }
}

export async function redisSet(key, value, ttlSeconds = 30) {
  const r = getRedis();
  if (!r) return;
  try {
    if (r.status !== 'ready' && r.status !== 'connecting') await r.connect();
    await r.set(key, value, 'EX', ttlSeconds);
  } catch {
    /* optional cache */
  }
}
