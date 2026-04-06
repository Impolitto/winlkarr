import bcrypt from 'bcryptjs';
import { User } from '../models/User.js';
import { AppError } from '../utils/AppError.js';
import { signToken } from '../utils/jwt.js';

const SALT_ROUNDS = 12;

export async function registerUser({ name, email, password, role = 'passenger' }) {
  if (role === 'admin') {
    throw new AppError('Cannot self-register as admin', 403);
  }
  const existing = await User.findOne({ email: email.toLowerCase() });
  if (existing) {
    throw new AppError('Email already registered', 409);
  }
  const hashed = await bcrypt.hash(password, SALT_ROUNDS);
  const user = await User.create({
    name,
    email,
    password: hashed,
    role,
  });
  const token = signToken({ sub: user._id.toString(), role: user.role });
  return { user: sanitizeUser(user), token };
}

export async function loginUser({ email, password }) {
  const user = await User.findOne({ email: email.toLowerCase() }).select(
    '+password'
  );
  if (!user) {
    throw new AppError('Invalid credentials', 401);
  }
  const ok = await bcrypt.compare(password, user.password);
  if (!ok) {
    throw new AppError('Invalid credentials', 401);
  }
  const token = signToken({ sub: user._id.toString(), role: user.role });
  return { user: sanitizeUser(user), token };
}

function sanitizeUser(user) {
  const o = user.toObject ? user.toObject() : { ...user };
  delete o.password;
  return o;
}
