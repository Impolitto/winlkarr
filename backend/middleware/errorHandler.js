export function errorHandler(err, req, res, next) {
  if (err.name === 'CastError') {
    return res.status(400).json({
      success: false,
      message: 'Invalid id format',
    });
  }
  if (err.name === 'ValidationError') {
    return res.status(400).json({
      success: false,
      message: Object.values(err.errors || {})
        .map((e) => e.message)
        .join(', ') || 'Validation error',
    });
  }

  const status = err.statusCode || 500;
  const message =
    status === 500 && process.env.NODE_ENV === 'production'
      ? 'Internal server error'
      : err.message || 'Internal server error';

  if (status === 500) {
    console.error(err);
  }

  res.status(status).json({
    success: false,
    message,
    ...(process.env.NODE_ENV !== 'production' && err.stack
      ? { stack: err.stack }
      : {}),
  });
}
