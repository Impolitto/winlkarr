import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as userController from '../controllers/userController.js';

const router = Router();

router.use(authenticate);

router.patch('/me', userController.updateMe);
router.delete('/me', userController.deleteMe);

router.get('/', authorize('admin'), userController.listUsers);
router.post('/', authorize('admin'), userController.createUser);
router.get('/:id', authorize('admin'), userController.getUser);
router.patch('/:id', authorize('admin'), userController.updateUser);
router.delete('/:id', authorize('admin'), userController.deleteUser);

export default router;
