import { Router } from 'express';
import * as authController from '../controllers/authController.js';
import { authenticate } from '../middleware/auth.js';
import { authLimiter } from '../middleware/rateLimiter.js';

const router = Router();

/**
 * @openapi
 * /auth/admin-register:
 *   post:
 *     tags: [Auth]
 *     summary: Create an admin account (requires ADMIN_REGISTRATION_SECRET)
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, email, password, secret]
 *             properties:
 *               name: { type: string }
 *               email: { type: string, format: email }
 *               password: { type: string, minLength: 6 }
 *               secret: { type: string }
 *     security: []
 *     responses:
 *       201:
 *         description: Created
 *       403:
 *         description: Invalid secret
 */
router.post('/admin-register', authLimiter, authController.adminRegister);

/**
 * @openapi
 * /auth/register:
 *   post:
 *     tags: [Auth]
 *     summary: Register a new user (passenger or driver)
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, email, password]
 *             properties:
 *               name: { type: string }
 *               email: { type: string, format: email }
 *               password: { type: string, minLength: 6 }
 *               role: { type: string, enum: [passenger, driver] }
 *     security: []
 *     responses:
 *       201:
 *         description: Created
 */
router.post('/register', authLimiter, authController.register);

/**
 * @openapi
 * /auth/login:
 *   post:
 *     tags: [Auth]
 *     summary: Login
 *     security: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [email, password]
 *             properties:
 *               email: { type: string }
 *               password: { type: string }
 *     responses:
 *       200:
 *         description: OK
 */
router.post('/login', authLimiter, authController.login);

/**
 * @openapi
 * /auth/me:
 *   get:
 *     tags: [Auth]
 *     summary: Current user
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: OK
 */
router.get('/me', authenticate, authController.me);

export default router;
