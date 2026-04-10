import 'dotenv/config';
import bcrypt from 'bcryptjs';
import mongoose from 'mongoose';
import { connectDatabase, disconnectDatabase } from '../config/database.js';
import { User } from '../models/User.js';
import { Bus } from '../models/Bus.js';
import { Trip } from '../models/Trip.js';
import { Station } from '../models/Station.js';

const SALT = 12;

async function seed() {
  await connectDatabase();

  await User.deleteMany({ email: /@seed\.local$/ });
  const oldSeedTrips = await Trip.find({ name: /^SEED / }).select('_id').lean();
  const oldIds = oldSeedTrips.map((t) => t._id);
  if (oldIds.length) {
    await Station.deleteMany({ tripId: { $in: oldIds } });
  }
  await Trip.deleteMany({ name: /^SEED / });
  await Bus.deleteMany({ busNumber: /^SEED-/ });

  await User.create({
    name: 'Seed Admin',
    email: 'admin@seed.local',
    password: await bcrypt.hash('Admin123!', SALT),
    role: 'admin',
  });

  const driver = await User.create({
    name: 'Seed Driver',
    email: 'driver@seed.local',
    password: await bcrypt.hash('Driver123!', SALT),
    role: 'driver',
  });

  await User.create({
    name: 'Seed Passenger',
    email: 'passenger@seed.local',
    password: await bcrypt.hash('Pass123!', SALT),
    role: 'passenger',
  });

  const bus = await Bus.create({
    busNumber: 'SEED-001',
    capacity: 40,
  });

  const trip = await Trip.create({
    name: 'SEED Downtown Express',
    from: 'Central Terminal',
    to: 'Airport',
    status: 'pending',
    driverId: driver._id,
    busId: bus._id,
    currentLocation: {
      type: 'Point',
      coordinates: [-73.935242, 40.73061],
    },
    currentStation: 'Central Terminal',
    nextStation: 'Market Square',
  });

  await Station.insertMany([
    {
      name: 'Central Terminal',
      lat: 40.73061,
      lng: -73.935242,
      order: 0,
      tripId: trip._id,
    },
    {
      name: 'Market Square',
      lat: 40.741,
      lng: -73.92,
      order: 1,
      tripId: trip._id,
    },
    {
      name: 'Airport',
      lat: 40.77,
      lng: -73.78,
      order: 2,
      tripId: trip._id,
    },
  ]);

  console.log('Seed complete.');
  console.log('Accounts (passwords shown for local dev only):');
  console.log('  admin@seed.local / Admin123!');
  console.log('  driver@seed.local / Driver123!');
  console.log('  passenger@seed.local / Pass123!');
  console.log('Trip ID:', trip._id.toString());

  await disconnectDatabase();
}

seed().catch((e) => {
  console.error(e);
  mongoose.connection.close().finally(() => process.exit(1));
});
