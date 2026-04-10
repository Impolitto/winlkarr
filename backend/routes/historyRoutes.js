import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as historyController from '../controllers/historyController.js';

const router = Router();

router.use(authenticate, authorize('passenger'));

router.get('/', historyController.listHistory);
router.post('/', historyController.addHistory);
router.delete('/:id', historyController.removeHistory);

export default router;
