import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as busController from '../controllers/busController.js';

const router = Router();

router.use(authenticate, authorize('admin'));

router.get('/', busController.listBuses);
router.post('/', busController.createBus);
router.get('/:id', busController.getBus);
router.patch('/:id', busController.updateBus);
router.delete('/:id', busController.deleteBus);

export default router;
