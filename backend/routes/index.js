import { Router } from 'express';
import authRoutes from './authRoutes.js';
import userRoutes from './userRoutes.js';
import busRoutes from './busRoutes.js';
import tripRoutes from './tripRoutes.js';
import stationRoutes from './stationRoutes.js';
import complaintRoutes from './complaintRoutes.js';
import notificationRoutes from './notificationRoutes.js';
import favoriteRoutes from './favoriteRoutes.js';
import historyRoutes from './historyRoutes.js';
import ratingRoutes from './ratingRoutes.js';

const router = Router();

router.use('/auth', authRoutes);
router.use('/users', userRoutes);
router.use('/buses', busRoutes);
router.use('/trips', tripRoutes);
router.use('/stations', stationRoutes);
router.use('/complaints', complaintRoutes);
router.use('/notifications', notificationRoutes);
router.use('/favorites', favoriteRoutes);
router.use('/history', historyRoutes);
router.use('/ratings', ratingRoutes);

export default router;
