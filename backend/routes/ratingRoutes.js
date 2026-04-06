import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as ratingController from '../controllers/ratingController.js';

const router = Router();

router.use(authenticate);

router.post('/', authorize('passenger'), ratingController.rateTrip);
router.get('/me', authorize('passenger'), ratingController.myRatings);
router.get('/trip/:tripId', ratingController.listTripRatings);

export default router;
