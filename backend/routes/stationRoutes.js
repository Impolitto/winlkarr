import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as stationController from '../controllers/stationController.js';

const router = Router();

router.get('/trip/:tripId', authenticate, stationController.listByTrip);

router.post('/', authenticate, authorize('admin'), stationController.createStation);
router.patch('/:id', authenticate, authorize('admin'), stationController.updateStation);
router.delete('/:id', authenticate, authorize('admin'), stationController.deleteStation);

export default router;
