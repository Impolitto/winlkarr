import { Router } from 'express';
import { authenticate } from '../middleware/auth.js';
import { authorize } from '../middleware/role.js';
import * as complaintController from '../controllers/complaintController.js';

const router = Router();

router.use(authenticate);

router.post('/', complaintController.createComplaint);
router.get('/me', complaintController.myComplaints);

router.get('/', authorize('admin'), complaintController.listAllComplaints);
router.patch('/:id', authorize('admin'), complaintController.updateComplaint);

export default router;
