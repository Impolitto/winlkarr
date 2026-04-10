/**
 * Aggregated OpenAPI path documentation (swagger-jsdoc).
 * @openapi
 * /health:
 *   get:
 *     tags: [System]
 *     summary: Health check
 *     security: []
 *     responses:
 *       200:
 *         description: OK
 *
 * /users/me:
 *   patch:
 *     tags: [Users]
 *     summary: Update own profile
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name: { type: string }
 *               email: { type: string }
 *               password: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   delete:
 *     tags: [Users]
 *     summary: Delete own account
 *     responses:
 *       200:
 *         description: OK
 *
 * /users:
 *   get:
 *     tags: [Users]
 *     summary: List users (admin)
 *     responses:
 *       200:
 *         description: OK
 *   post:
 *     tags: [Users]
 *     summary: Create user (admin)
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, email, password, role]
 *             properties:
 *               name: { type: string }
 *               email: { type: string }
 *               password: { type: string }
 *               role: { type: string, enum: [passenger, driver, admin] }
 *     responses:
 *       201:
 *         description: Created
 *
 * /users/{id}:
 *   get:
 *     tags: [Users]
 *     summary: Get user (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   patch:
 *     tags: [Users]
 *     summary: Update user (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   delete:
 *     tags: [Users]
 *     summary: Delete user (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /buses:
 *   get:
 *     tags: [Buses]
 *     summary: List buses
 *     responses:
 *       200:
 *         description: OK
 *   post:
 *     tags: [Buses]
 *     summary: Create bus
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [busNumber, capacity]
 *             properties:
 *               busNumber: { type: string }
 *               capacity: { type: integer }
 *     responses:
 *       201:
 *         description: Created
 *
 * /buses/{id}:
 *   get:
 *     tags: [Buses]
 *     summary: Get bus
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   patch:
 *     tags: [Buses]
 *     summary: Update bus
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   delete:
 *     tags: [Buses]
 *     summary: Delete bus
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips:
 *   get:
 *     tags: [Trips]
 *     summary: List trips (admin)
 *     responses:
 *       200:
 *         description: OK
 *   post:
 *     tags: [Trips]
 *     summary: Create trip (admin)
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, from, to, busId]
 *             properties:
 *               name: { type: string }
 *               from: { type: string }
 *               to: { type: string }
 *               busId: { type: string }
 *               driverId: { type: string }
 *               status: { type: string, enum: [pending, active, completed] }
 *               currentLocation:
 *                 type: object
 *                 properties:
 *                   lat: { type: number }
 *                   lng: { type: number }
 *     responses:
 *       201:
 *         description: Created
 *
 * /trips/active:
 *   get:
 *     tags: [Trips]
 *     summary: Active trips (passengers)
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips/nearest:
 *   get:
 *     tags: [Trips]
 *     summary: Nearest active buses (Geo query)
 *     parameters:
 *       - in: query
 *         name: lat
 *         required: true
 *         schema: { type: number }
 *       - in: query
 *         name: lng
 *         required: true
 *         schema: { type: number }
 *       - in: query
 *         name: maxDistance
 *         schema: { type: number, description: meters, default: 5000 }
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips/{id}:
 *   get:
 *     tags: [Trips]
 *     summary: Get trip
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   patch:
 *     tags: [Trips]
 *     summary: Update trip (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   delete:
 *     tags: [Trips]
 *     summary: Delete trip (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips/{id}/station-etas:
 *   get:
 *     tags: [Trips]
 *     summary: ETA to each station
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *       - in: query
 *         name: avgSpeedKmh
 *         schema: { type: number, default: 25 }
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips/{id}/start:
 *   patch:
 *     tags: [Trips]
 *     summary: Start trip (assigned driver)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips/{id}/location:
 *   patch:
 *     tags: [Trips]
 *     summary: Update GPS (driver)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [lat, lng]
 *             properties:
 *               lat: { type: number }
 *               lng: { type: number }
 *               currentStation: { type: string }
 *               nextStation: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips/{id}/status:
 *   patch:
 *     tags: [Trips]
 *     summary: Update trip status (driver)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               status: { type: string, enum: [pending, active, completed] }
 *     responses:
 *       200:
 *         description: OK
 *
 * /trips/{id}/end:
 *   patch:
 *     tags: [Trips]
 *     summary: End trip (driver)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /stations/trip/{tripId}:
 *   get:
 *     tags: [Stations]
 *     summary: Stations for a trip
 *     security: [{ bearerAuth: [] }]
 *     parameters:
 *       - in: path
 *         name: tripId
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /stations:
 *   post:
 *     tags: [Stations]
 *     summary: Create station (admin)
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, lat, lng, order, tripId]
 *             properties:
 *               name: { type: string }
 *               lat: { type: number }
 *               lng: { type: number }
 *               order: { type: integer }
 *               tripId: { type: string }
 *     responses:
 *       201:
 *         description: Created
 *
 * /stations/{id}:
 *   patch:
 *     tags: [Stations]
 *     summary: Update station (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *   delete:
 *     tags: [Stations]
 *     summary: Delete station (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /complaints:
 *   post:
 *     tags: [Complaints]
 *     summary: Create complaint (passenger/driver)
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [subject, message]
 *             properties:
 *               tripId: { type: string }
 *               subject: { type: string }
 *               message: { type: string }
 *               priority: { type: string, enum: [low, medium, high] }
 *               category: { type: string, enum: [delay, driver, bus, other] }
 *     responses:
 *       201:
 *         description: Created
 *   get:
 *     tags: [Complaints]
 *     summary: List all complaints (admin)
 *     parameters:
 *       - in: query
 *         name: status
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /complaints/me:
 *   get:
 *     tags: [Complaints]
 *     summary: My complaints
 *     responses:
 *       200:
 *         description: OK
 *
 * /complaints/{id}:
 *   patch:
 *     tags: [Complaints]
 *     summary: Update complaint (admin)
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               status: { type: string, enum: [pending, in_progress, resolved] }
 *               response: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /notifications:
 *   get:
 *     tags: [Notifications]
 *     summary: List notifications
 *     parameters:
 *       - in: query
 *         name: read
 *         schema: { type: string, enum: [true, false] }
 *     responses:
 *       200:
 *         description: OK
 *
 * /notifications/read-all:
 *   post:
 *     tags: [Notifications]
 *     summary: Mark all read
 *     responses:
 *       200:
 *         description: OK
 *
 * /notifications/{id}/read:
 *   patch:
 *     tags: [Notifications]
 *     summary: Mark one read
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /favorites:
 *   get:
 *     tags: [Favorites]
 *     summary: List favorites
 *     responses:
 *       200:
 *         description: OK
 *   post:
 *     tags: [Favorites]
 *     summary: Add favorite trip
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [tripId]
 *             properties:
 *               tripId: { type: string }
 *     responses:
 *       201:
 *         description: Created
 *
 * /favorites/{tripId}:
 *   delete:
 *     tags: [Favorites]
 *     summary: Remove favorite
 *     parameters:
 *       - in: path
 *         name: tripId
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /history:
 *   get:
 *     tags: [History]
 *     summary: Trip history
 *     responses:
 *       200:
 *         description: OK
 *   post:
 *     tags: [History]
 *     summary: Add history entry
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [tripId]
 *             properties:
 *               tripId: { type: string }
 *               note: { type: string }
 *     responses:
 *       201:
 *         description: Created
 *
 * /history/{id}:
 *   delete:
 *     tags: [History]
 *     summary: Remove history entry
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 *
 * /ratings:
 *   post:
 *     tags: [Ratings]
 *     summary: Rate a trip (passenger)
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [tripId, score]
 *             properties:
 *               tripId: { type: string }
 *               score: { type: integer, minimum: 1, maximum: 5 }
 *               comment: { type: string }
 *     responses:
 *       201:
 *         description: Created
 *
 * /ratings/me:
 *   get:
 *     tags: [Ratings]
 *     summary: My ratings
 *     responses:
 *       200:
 *         description: OK
 *
 * /ratings/trip/{tripId}:
 *   get:
 *     tags: [Ratings]
 *     summary: Ratings for a trip
 *     parameters:
 *       - in: path
 *         name: tripId
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200:
 *         description: OK
 */
export const swaggerPathsPlaceholder = true;
