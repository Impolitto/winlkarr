import bcrypt from 'bcryptjs';
import { asyncHandler } from '../utils/asyncHandler.js';
import { AppError } from '../utils/AppError.js';
import { User } from '../models/User.js';

const SALT_ROUNDS = 12;

export const listUsers = asyncHandler(async (req, res) => {
  const users = await User.find().select('-password').sort({ createdAt: -1 });
  res.json({ success: true, data: users });
});

export const getUser = asyncHandler(async (req, res) => {
  const user = await User.findById(req.params.id).select('-password');
  if (!user) throw new AppError('User not found', 404);
  res.json({ success: true, data: user });
});

export const createUser = asyncHandler(async (req, res) => {
  const { name, email, password, role } = req.body;
  const existing = await User.findOne({ email: email.toLowerCase() });
  if (existing) throw new AppError('Email already registered', 409);
  const hashed = await bcrypt.hash(password, SALT_ROUNDS);
  const user = await User.create({
    name,
    email,
    password: hashed,
    role: role || 'passenger',
  });
  res.status(201).json({
    success: true,
    data: { user: { ...user.toObject(), password: undefined } },
  });
});

export const updateUser = asyncHandler(async (req, res) => {
  const { name, email, password, role } = req.body;
  const user = await User.findById(req.params.id);
  if (!user) throw new AppError('User not found', 404);

  if (email && email.toLowerCase() !== user.email) {
    const taken = await User.findOne({ email: email.toLowerCase() });
    if (taken) throw new AppError('Email already in use', 409);
    user.email = email;
  }
  if (name) user.name = name;
  if (role) user.role = role;
  if (password) user.password = await bcrypt.hash(password, SALT_ROUNDS);
  await user.save();
  res.json({
    success: true,
    data: { user: { ...user.toObject(), password: undefined } },
  });
});

export const deleteUser = asyncHandler(async (req, res) => {
  if (req.params.id === req.user._id.toString()) {
    throw new AppError('Cannot delete your own account this way', 400);
  }
  const user = await User.findByIdAndDelete(req.params.id);
  if (!user) throw new AppError('User not found', 404);
  res.json({ success: true, message: 'User deleted' });
});

export const updateMe = asyncHandler(async (req, res) => {
  const { name, email, password } = req.body;
  const user = await User.findById(req.user._id).select('+password');
  if (email && email.toLowerCase() !== user.email) {
    const taken = await User.findOne({ email: email.toLowerCase() });
    if (taken) throw new AppError('Email already in use', 409);
    user.email = email;
  }
  if (name) user.name = name;
  if (password) user.password = await bcrypt.hash(password, SALT_ROUNDS);
  await user.save();
  const out = user.toObject();
  delete out.password;
  res.json({ success: true, data: { user: out } });
});

export const deleteMe = asyncHandler(async (req, res) => {
  await User.findByIdAndDelete(req.user._id);
  res.json({ success: true, message: 'Account deleted' });
});
