import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as tripController from '../controllers/tripController.js';

const router = Router();

router.use(authenticate);

router.get('/active', tripController.listActiveTrips);
router.get('/nearest', tripController.nearestBuses);

router.get('/', authorize('admin'), tripController.listTrips);
router.post('/', authorize('admin'), tripController.createTrip);

router.get('/:id/station-etas', tripController.tripStationEtas);

router.patch('/:id/start', authorize('driver'), tripController.startTrip);
router.patch('/:id/location', authorize('driver'), tripController.updateTripLocation);
router.patch('/:id/status', authorize('driver'), tripController.updateTripStatusDriver);
router.patch('/:id/end', authorize('driver'), tripController.endTrip);

router.get('/:id', tripController.getTrip);
router.patch('/:id', authorize('admin'), tripController.updateTrip);
router.delete('/:id', authorize('admin'), tripController.deleteTrip);

export default router;
