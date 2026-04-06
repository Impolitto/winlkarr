import path from 'path';
import { fileURLToPath } from 'url';
import swaggerJsdoc from 'swagger-jsdoc';
import swaggerUi from 'swagger-ui-express';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.join(__dirname, '..');
const posix = (p) => p.split(path.sep).join('/');
const port = process.env.PORT || 4000;

const options = {
  definition: {
    openapi: '3.0.3',
    info: {
      title: 'Bus Tracking API',
      version: '1.0.0',
      description:
        'REST API for bus tracking, trips, stations, complaints, and real-time updates via Socket.IO.',
    },
    servers: [
      {
        url: `http://localhost:${port}/api`,
        description: 'Local',
      },
    ],
    tags: [
      { name: 'System', description: 'Health and status' },
      { name: 'Auth', description: 'Registration and login' },
      { name: 'Users', description: 'User management' },
      { name: 'Buses', description: 'Bus fleet (admin)' },
      { name: 'Trips', description: 'Trips and GPS' },
      { name: 'Stations', description: 'Stops along a trip' },
      { name: 'Complaints', description: 'Support tickets' },
      { name: 'Notifications', description: 'In-app notifications' },
      { name: 'Favorites', description: 'Saved trips (passenger)' },
      { name: 'History', description: 'Trip history (passenger)' },
      { name: 'Ratings', description: 'Trip ratings' },
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
        },
      },
    },
    security: [{ bearerAuth: [] }],
  },
  apis: [
    posix(path.join(rootDir, 'routes', '*.js')),
    posix(path.join(rootDir, 'docs', 'swaggerPaths.js')),
  ],
};

export const swaggerSpec = swaggerJsdoc(options);

export const swaggerUiMiddleware = swaggerUi.serve;
export const swaggerUiSetup = swaggerUi.setup(swaggerSpec, {
  explorer: true,
  customCss: '.swagger-ui .topbar { display: none }',
});
