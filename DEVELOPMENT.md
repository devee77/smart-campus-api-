# Smart Campus Development Log

## April 9: Day 1 - Initial Setup
- Created JAX-RS application with @ApplicationPath annotation
- Implemented Room, Sensor, and SensorReading data models

## April 11: Day 3 - Discovery Endpoint
- Implemented GET /api/v1 discovery endpoint with HATEOAS

## April 13: Day 5 - Room Management
- Implemented GET /api/v1/rooms endpoint
- Implemented POST /api/v1/rooms for room creation

## April 15: Day 7 - Room Deletion Safety
- Implemented DELETE /api/v1/rooms/{roomId}
- Added RoomNotEmptyException for rooms with active sensors

## April 17: Day 9 - Sensor Operations
- Implemented POST /api/v1/sensors with room validation
- Implemented GET /api/v1/sensors with optional @QueryParam filter

## April 19: Day 11 - Sub-Resource Pattern
- Implemented sub-resource locator for /sensors/{id}/readings
- Created SensorReadingResource class for reading management

## April 21: Day 13 - Error Handling
- Implemented all exception mappers (409, 422, 403, 500)
- All mappers return JSON error responses

## April 23: Day 15 - Logging & Observability
- Implemented ApiLoggingFilter with request/response logging
- Project complete and ready for submission

✓ Discovery endpoint
✓ Room CRUD endpoints
✓ Room deletion safety logic
✓ Sensor operations and filtering
✓ Sub-resource pattern and reading history
