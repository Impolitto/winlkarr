import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import * as notificationController from '../controllers/notificationController.js';

const router = Router();

router.use(authenticate);

router.get('/', notificationController.listNotifications);
router.patch('/:id/read', notificationController.markOneRead);
router.post('/read-all', notificationController.markAllRead);

export default router;
