import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as favoriteController from '../controllers/favoriteController.js';

const router = Router();

router.use(authenticate, authorize('passenger'));

router.get('/', favoriteController.listFavorites);
router.post('/', favoriteController.addFavorite);
router.delete('/:tripId', favoriteController.removeFavorite);

export default router;
