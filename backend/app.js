import express from 'express';
import helmet from 'helmet';
import cors from 'cors';
import routes from './routes/index.js';
import { errorHandler } from './middleware/errorHandler.js';
import { globalLimiter } from './middleware/rateLimiter.js';
import { swaggerUiMiddleware, swaggerUiSetup } from './config/swagger.js';

export const app = express();

const corsOrigin = process.env.CORS_ORIGIN ?? '*';
app.use(
  cors({
    origin:
      corsOrigin === '*'
        ? true
        : corsOrigin.split(',').map((o) => o.trim()),
    credentials: true,
  })
);
app.use(helmet());
app.use(express.json({ limit: '1mb' }));
app.use(globalLimiter);

app.get('/api/health', (req, res) => {
  res.json({ ok: true, service: 'bus-tracking-api' });
});

app.use('/api/docs', swaggerUiMiddleware, swaggerUiSetup);
app.use('/api', routes);

app.use((req, res) => {
  res.status(404).json({ success: false, message: 'Not found' });
});

app.use(errorHandler);
