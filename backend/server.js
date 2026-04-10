import 'dotenv/config';
import { createServer } from 'http';
import { app } from './app.js';
import { connectDatabase } from './config/database.js';
import { initSocket } from './sockets/index.js';

const port = Number(process.env.PORT) || 4000;

async function start() {
  await connectDatabase();
  const server = createServer(app);
  initSocket(server);

  server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
      console.error(
        `Port ${port} is already in use. Close the other Node process using it, or set PORT in .env to a free port (e.g. 4001).`
      );
      process.exit(1);
      return;
    }
    console.error('Server failed to start:', err);
    process.exit(1);
  });

  server.listen(port, () => {
    console.log(`HTTP + Socket.IO on port ${port}`);
    console.log(`Swagger UI: http://localhost:${port}/api/docs`);
  });
}

start().catch((err) => {
  console.error(err);
  process.exit(1);
});
