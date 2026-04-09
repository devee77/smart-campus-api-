# Smart Campus: Sensor & Room Management API

A comprehensive RESTful API built with JAX-RS for managing sensors and rooms across a university campus. This project implements industry-standard REST principles and robust error handling patterns.

## Project Overview

The Smart Campus API provides a high-performance web service for campus facilities managers and automated building systems to interact with campus infrastructure data. It manages thousands of Rooms and diverse array of Sensors (temperature monitors, CO2 trackers, occupancy sensors, smart lighting controllers, etc.).

**Course**: Client-Server Architectures (5COSC022W)  
**Institution**: University of Westminster  
**Module Leader**: Hamed Hamzeh  
**Weighting**: 60% of final grade  
**Due Date**: 24th April 2026, 13:00

## Key Features

- **Room Management**: Full CRUD operations for campus rooms with capacity tracking
- **Sensor Integration**: Create and manage sensors with real-time status monitoring
- **Reading History**: Temporal tracking of sensor readings with historical data access
- **Nested Resources**: Sub-resource locator pattern for organizing hierarchical data
- **Comprehensive Error Handling**: Custom exception mappers with semantically correct HTTP status codes
- **Request/Response Logging**: Cross-cutting observability through filters
- **RESTful Design**: HATEOAS support with discovery endpoints

## Technology Stack

- **Framework**: JAX-RS (Jakarta RESTful Web Services)
- **Server**: GlassFish / Jersey
- **Language**: Java 11+
- **Build Tool**: Maven
- **Data Storage**: In-memory data structures (HashMap, ArrayList)

## Project Structure

```
smart-campus/
├── src/
│   ├── main/
│   │   ├── java/com/smartcampus/
│   │   │   ├── application/         # JAX-RS Application class
│   │   │   ├── controller/          # Resource classes
│   │   │   │   ├── DiscoveryController.java
│   │   │   │   ├── RoomController.java
│   │   │   │   ├── SensorController.java
│   │   │   │   └── SensorReadingController.java
│   │   │   ├── exception/           # Custom exceptions & mappers
│   │   │   ├── filter/              # Request/response logging filter
│   │   │   ├── model/               # Domain models (POJOs)
│   │   │   ├── repository/          # Data access layer
│   │   │   └── service/             # Business logic layer
│   │   └── webapp/
│   │       ├── index.jsp
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── context.xml
│   └── pom.xml                      # Maven configuration
```

## Build & Deployment Instructions

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- GlassFish Server (or any JAX-RS compatible container)

### Step 1: Clone the Repository

```bash
git clone https://github.com/devee77/smart-campus-api-.git
cd smart-campus
```

### Step 2: Build the Project

```bash
mvn clean install
```

This will:
- Compile the Java sources
- Run any unit tests (if present)
- Package the application as a WAR file
- Output: `target/smart-campus-api-1.0.0.war`

### Step 3: Deploy to GlassFish

```bash
# Copy the WAR to GlassFish deployments directory
cp target/smart-campus-api-1.0.0.war /path/to/glassfish/domains/domain1/autodeploy/

# Or use the asadmin command
asadmin deploy --contextroot /smart-campus target/smart-campus-api-1.0.0.war
```

### Step 4: Verify the Server is Running

The API will be available at: `http://localhost:8080/smart-campus/api/v1`

## API Documentation

### 1. Discovery Endpoint

Provides API metadata and navigation information.

```bash
curl -X GET http://localhost:8080/smart-campus/api/v1
```

**Response (200 OK):**
```json
{
  "version": "1.0.0",
  "timestamp": "2026-04-24T10:30:00Z",
  "contact": "facilities@university.ac.uk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Room Management

#### Get All Rooms

```bash
curl -X GET http://localhost:8080/smart-campus/api/v1/rooms
```

**Response (200 OK):**
```json
[
  {
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50,
    "sensorIds": ["TEMP-001", "CO2-002"]
  },
  {
    "id": "LAB-201",
    "name": "Computer Science Lab",
    "capacity": 40,
    "sensorIds": ["OCC-003"]
  }
]
```

#### Create a New Room

```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50,
    "sensorIds": []
  }'
```

**Response (201 Created):**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

#### Get Specific Room

```bash
curl -X GET http://localhost:8080/smart-campus/api/v1/rooms/LIB-301
```

**Response (200 OK):**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": ["TEMP-001", "CO2-002"]
}
```

#### Delete a Room

```bash
curl -X DELETE http://localhost:8080/smart-campus/api/v1/rooms/LIB-301
```

**Response (204 No Content)** - if successful

**Response (409 Conflict)** - if room has active sensors:
```json
{
  "status": 409,
  "error": "RoomNotEmptyException",
  "message": "Cannot delete room LIB-301: it contains 2 active sensor(s)"
}
```

---

### 3. Sensor Management

#### Create a New Sensor

```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
  }'
```

**Response (201 Created):**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "LIB-301"
}
```

**Response (422 Unprocessable Entity)** - if roomId doesn't exist:
```json
{
  "status": 422,
  "error": "LinkedResourceNotFoundException",
  "message": "Referenced room 'LIB-999' does not exist in the system"
}
```

#### Get All Sensors (with optional filtering)

```bash
# Get all sensors
curl -X GET http://localhost:8080/smart-campus/api/v1/sensors

# Filter by type
curl -X GET http://localhost:8080/smart-campus/api/v1/sensors?type=Temperature
```

**Response (200 OK):**
```json
[
  {
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
  },
  {
    "id": "CO2-002",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 450,
    "roomId": "LIB-301"
  }
]
```

---

### 4. Sensor Readings (Sub-Resource)

#### Get Reading History for a Sensor

```bash
curl -X GET http://localhost:8080/smart-campus/api/v1/sensors/TEMP-001/readings
```

**Response (200 OK):**
```json
[
  {
    "id": "read-uuid-001",
    "timestamp": 1713960000000,
    "value": 22.5
  },
  {
    "id": "read-uuid-002",
    "timestamp": 1713960300000,
    "value": 22.7
  }
]
```

#### Add a New Reading

```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "id": "read-uuid-003",
    "timestamp": 1713960600000,
    "value": 23.1
  }'
```

**Response (201 Created):**
```json
{
  "id": "read-uuid-003",
  "timestamp": 1713960600000,
  "value": 23.1
}
```

**Response (403 Forbidden)** - if sensor is in MAINTENANCE:
```json
{
  "status": 403,
  "error": "SensorUnavailableException",
  "message": "Sensor TEMP-001 is currently in MAINTENANCE status and cannot accept readings"
}
```

---

## Error Handling

The API implements comprehensive error handling with meaningful HTTP status codes and JSON responses:

| HTTP Status | Exception | Scenario |
|-------------|-----------|----------|
| 400 | InvalidRequestException | Malformed request body |
| 404 | ResourceNotFoundException | Resource not found |
| 409 | RoomNotEmptyException | Deleting room with active sensors |
| 422 | LinkedResourceNotFoundException | Foreign key reference doesn't exist |
| 403 | SensorUnavailableException | Sensor in MAINTENANCE/OFFLINE status |
| 500 | Global ExceptionMapper | Unexpected runtime errors |

All error responses follow this format:
```json
{
  "status": 409,
  "error": "ExceptionClassName",
  "message": "Human-readable error description",
  "timestamp": "2026-04-24T10:30:00Z"
}
```

---

## Architectural Design & Implementation Report

### Part 1: Service Architecture & Setup

#### 1.1 Project Configuration & JAX-RS Application Lifecycle

The project is bootstrapped as a Maven-based JAX-RS implementation using Jersey, deployed on GlassFish. The `@ApplicationPath("/api/v1")` annotation establishes a versioned API entry point.

**JAX-RS Resource Lifecycle:**

By default, JAX-RS instantiates a new resource class instance for **every incoming request**. However, this behavior can be modified through scope annotations:

- **@RequestScoped** (Default): New instance per request
- **@ApplicationScoped**: Single singleton instance
- **@SessionScoped**: Instance per session

**Impact on In-Memory Data Management:**

Since we use a singleton `DataStore` with HashMaps and ArrayLists, all requests share the same data structures. This necessitates thread-safe design:

1. **Synchronization Strategy**: The `DataStore` repository uses `Collections.synchronizedMap()` to wrap HashMaps, preventing race conditions when multiple requests access/modify data simultaneously.

2. **Data Consistency**: POST operations that create rooms/sensors update the shared maps. GET operations read from these shared structures. DELETE operations must verify constraints before modifying shared state.

3. **Example Race Condition Avoided**: If two concurrent requests try to delete the same room, synchronization ensures only one proceeds; the second receives a 404.

#### 1.2 Discovery Endpoint

The Discovery endpoint (`GET /api/v1`) returns HATEOAS-compliant metadata:

```json
{
  "version": "1.0.0",
  "timestamp": "2026-04-24T10:30:00Z",
  "contact": "facilities@university.ac.uk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors",
    "docs": "https://github.com/devee77/smart-campus-api-/blob/main/README.md"
  }
}
```

**Why HATEOAS Matters:**

HATEOAS (Hypermedia As The Engine Of Application State) is a core REST constraint that enhances API maturity:

1. **Self-Discovering**: Clients don't need hardcoded URLs; navigation links are embedded in responses.
2. **Resilience**: API URLs can change without breaking clients (as long as link relations remain stable).
3. **Discoverability**: New developers understand the API structure by exploring from the root endpoint.
4. **Evolution**: New resources can be added without breaking existing clients that ignore unknown links.

**Comparison to Static Documentation:**
- Static docs are decoupled from the API; changes aren't automatically reflected
- HATEOAS keeps documentation and implementation synchronized
- Reduces client-side parsing logic; clients follow hypermedia links programmatically

---

### Part 2: Room Management

#### 2.1 Room Resource Implementation

The `RoomController` (resource class) manages the `/api/v1/rooms` path:

- **GET /**: Returns `List<Room>` with all rooms
- **POST /**: Creates a new room, validates uniqueness of room ID
- **GET /{roomId}**: Returns specific room or 404 if not found

**Implications of Returning Full Objects vs. IDs:**

When listing rooms, returning full objects (`List<Room>`) instead of just IDs has trade-offs:

**Full Objects Approach:**
- ✓ Reduces client requests (no need for N+1 GET calls)
- ✓ Provides complete context in one response
- ✗ Increases bandwidth (especially with many rooms)
- ✗ May expose sensitive room metadata

**IDs-Only Approach:**
- ✓ Minimal bandwidth for list retrieval
- ✓ Clients fetch only rooms they're interested in
- ✗ Requires additional GET requests per room (N+1 problem)
- ✗ Slower user experience for UI clients

**Our Implementation**: We return full objects to optimize client-side processing and reduce round-trips, accepting the bandwidth trade-off for campus-scale deployments (thousands of rooms).

#### 2.2 Room Deletion & Safety Logic

The DELETE handler prevents deletion of rooms with active sensors:

```java
if (!room.getSensorIds().isEmpty()) {
    throw new RoomNotEmptyException("Cannot delete room: contains active sensors");
}
```

**Is DELETE Idempotent?**

Yes, our DELETE implementation is **idempotent**:

1. **First DELETE /rooms/LIB-301**: Room exists → deleted → returns 204 No Content
2. **Second DELETE /rooms/LIB-301**: Room doesn't exist → returns 404 Not Found (or 204 in idempotent design)

**Idempotence Definition**: An operation is idempotent if calling it multiple times produces the same result as calling it once. Our implementation treats repeated DELETEs of non-existent resources as successful (204), making it truly idempotent.

**Design Choice**: Some APIs return 404 for already-deleted resources. We return 204 for true idempotence, allowing clients to safely retry failed DELETE requests without worrying about resource state changes.

---

### Part 3: Sensor Operations & Linking

#### 3.1 Sensor Resource & Integrity Constraints

The `SensorController` manages `/api/v1/sensors`:

**POST Validation:**
```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response createSensor(Sensor sensor) {
    if (!roomService.roomExists(sensor.getRoomId())) {
        throw new LinkedResourceNotFoundException(
            "Referenced room '" + sensor.getRoomId() + "' does not exist"
        );
    }
    // ... create sensor
}
```

**Technical Consequences of @Consumes Mismatch:**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation enforces content-type validation:

- **Client sends JSON**: ✓ Processed normally
- **Client sends text/plain**: JAX-RS returns **HTTP 415 Unsupported Media Type**
- **Client sends application/xml**: JAX-RS returns **HTTP 415 Unsupported Media Type**

**JAX-RS Handling:**
1. Request arrives with `Content-Type: text/plain`
2. Jersey compares against `@Consumes(APPLICATION_JSON)`
3. Mismatch detected → Response filter adds 415 status
4. Response sent to client before method body executes

This prevents malformed data from reaching business logic, enforcing client compliance at the framework level.

#### 3.2 Filtered Retrieval & Search

The GET endpoint supports optional `type` parameter:

```java
@GET
public Response listSensors(@QueryParam("type") String type) {
    List<Sensor> sensors = sensorService.getAllSensors();
    if (type != null) {
        sensors = sensors.stream()
            .filter(s -> s.getType().equalsIgnoreCase(type))
            .collect(Collectors.toList());
    }
    return Response.ok(sensors).build();
}
```

**Query Parameter vs. Path-Based Filtering:**

Alternative design: `GET /api/v1/sensors/type/CO2`

**Why Query Parameters are Superior for Filtering:**

1. **Semantic Correctness**: Query parameters indicate filtering/querying; path segments indicate resource hierarchy
   - `/sensors?type=CO2` → "Query sensors filtered by type"
   - `/sensors/type/CO2` → "Access type 'CO2' resource" (misleading)

2. **Multiple Filters**: Query params scale elegantly:
   ```
   GET /sensors?type=Temperature&status=ACTIVE&roomId=LIB-301
   ```
   Path-based would be unwieldy: `/sensors/type/Temperature/status/ACTIVE/room/LIB-301`

3. **Caching**: Servers treat query strings inconsistently for caching; path-based URLs are cached more predictably

4. **REST Maturity**: Richardson Maturity Model recognizes query parameters as proper filtering mechanism

5. **HTTP Semantics**: GET parameters don't modify state; path segments imply accessing specific resources

Our implementation correctly uses `@QueryParam("type")` for semantic accuracy and scalability.

---

### Part 4: Deep Nesting with Sub-Resources

#### 4.1 Sub-Resource Locator Pattern

The `SensorController` implements a sub-resource locator:

```java
@Path("{sensorId}/readings")
public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId, readingService);
}
```

This delegates `/sensors/{sensorId}/readings` operations to `SensorReadingResource`.

**Architectural Benefits:**

1. **Separation of Concerns**: Each resource class handles one domain entity. `SensorController` manages sensors; `SensorReadingResource` manages readings.

2. **Reduced Complexity**: Without delegation, a single class would need hundreds of lines handling multiple nested paths. Sub-resources keep classes focused and readable.

3. **Scalability**: New nested resources can be added (e.g., `/sensors/{id}/calibration`) by creating new sub-resource classes without modifying existing code.

4. **Testability**: Small, focused classes are easier to unit test. Sub-resource classes can be tested independently.

5. **Reusability**: `SensorReadingResource` could be reused if other entities had readings.

**Comparison to Monolithic Approach:**

Defining all paths in one controller:
```java
@GET
@Path("/sensors/{sensorId}/readings/{readingId}")
public Reading getReading(@PathParam("sensorId") String sensorId, 
                          @PathParam("readingId") String readingId) { ... }

@POST
@Path("/sensors/{sensorId}/readings")
public Response createReading(@PathParam("sensorId") String sensorId, 
                              Reading reading) { ... }
```

Results in:
- ✗ Single massive controller with 100+ methods
- ✗ Hard to maintain and understand
- ✗ No clear responsibility boundaries
- ✗ Difficult to reuse logic across APIs

Our sub-resource approach provides industry-standard delegation.

#### 4.2 Historical Data Management

`SensorReadingResource` implements GET and POST:

```java
@GET
public Response getReadings() {
    List<SensorReading> readings = readingService.getReadingsForSensor(sensorId);
    return Response.ok(readings).build();
}

@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response addReading(SensorReading reading) {
    reading = readingService.recordReading(sensorId, reading);
    
    // Side effect: Update sensor's currentValue
    Sensor sensor = sensorService.getSensor(sensorId);
    sensor.setCurrentValue(reading.getValue());
    
    return Response.status(201).entity(reading).build();
}
```

**Side Effect - Data Consistency:**

When a reading is POSTed, the parent Sensor's `currentValue` is immediately updated. This ensures:

1. **Consistency**: Sensor always reflects its most recent reading
2. **Query Efficiency**: No need to query all readings to get current value
3. **Atomicity**: Reading creation and sensor update happen in single transaction

**Example Flow:**
```
POST /sensors/TEMP-001/readings with value 23.5
  → Creates SensorReading entry
  → Updates Sensor TEMP-001's currentValue to 23.5
  → Next GET /sensors/TEMP-001 reflects new value immediately
```

---

### Part 5: Advanced Error Handling & Logging

#### 5.1 Resource Conflict (409 Conflict)

**Scenario**: Deleting a room with active sensors

**Implementation:**

```java
public class RoomNotEmptyException extends Exception {
    private String roomId;
    private int sensorCount;
    
    public RoomNotEmptyException(String roomId, int sensorCount) {
        this.roomId = roomId;
        this.sensorCount = sensorCount;
    }
}

@Provider
public class RoomNotEmptyExceptionMapper 
    implements ExceptionMapper<RoomNotEmptyException> {
    
    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ErrorResponse error = new ErrorResponse(
            409,
            "RoomNotEmptyException",
            "Cannot delete room " + ex.getRoomId() + 
            ": it contains " + ex.getSensorCount() + " active sensor(s)",
            System.currentTimeMillis()
        );
        return Response.status(409).entity(error).build();
    }
}
```

**Why 409 Conflict?** The room exists and is valid, but its state (having sensors) conflicts with the requested action. 409 is semantically perfect for state-based rejections.

#### 5.2 Dependency Validation (422 Unprocessable Entity)

**Scenario**: POST sensor with non-existent roomId

```java
@Provider
public class LinkedResourceNotFoundExceptionMapper 
    implements ExceptionMapper<LinkedResourceNotFoundException> {
    
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            422,
            "LinkedResourceNotFoundException",
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return Response.status(422).entity(error).build();
    }
}
```

**422 vs. 404 Semantics:**

- **404 Not Found**: The referenced URL doesn't exist (e.g., GET /sensors/FAKE-001)
- **422 Unprocessable Entity**: The request body contains valid JSON, but a reference within it is broken

When posting `{"roomId": "LIB-999"}` where LIB-999 doesn't exist:
- The JSON is syntactically valid → not a 400
- The entity exists (we're creating a sensor) but can't be processed due to broken reference → 422
- The roomId resource would be at `/rooms/LIB-999` (404 if accessed directly), but that's different from a validation error in a POST body

**Our Implementation**: Returns 422 to distinguish "invalid data content" from "resource not found."

#### 5.3 State Constraint (403 Forbidden)

**Scenario**: Posting reading to MAINTENANCE sensor

```java
@Provider
public class SensorUnavailableExceptionMapper 
    implements ExceptionMapper<SensorUnavailableException> {
    
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ErrorResponse error = new ErrorResponse(
            403,
            "SensorUnavailableException",
            "Sensor " + ex.getSensorId() + 
            " is currently in " + ex.getStatus() + 
            " status and cannot accept readings",
            System.currentTimeMillis()
        );
        return Response.status(403).entity(error).build();
    }
}
```

**403 Forbidden**: The client is authenticated but lacks permission/access due to resource state. Perfect for "sensor under maintenance" scenarios.

#### 5.4 Global Safety Net (500 Internal Server Error)

```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOGGER = 
        Logger.getLogger(GlobalExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.log(Level.SEVERE, "Unexpected error", ex);
        
        ErrorResponse error = new ErrorResponse(
            500,
            "InternalServerError",
            "An unexpected error occurred. Please contact support.",
            System.currentTimeMillis()
        );
        
        return Response.status(500).entity(error).build();
    }
}
```

**Security Implications of Stack Traces:**

Exposing Java stack traces to API consumers creates security risks:

1. **Information Disclosure**: Stack traces reveal:
   - Class names and package structure (internal architecture)
   - Database URLs and connection strings (if in logs)
   - Library versions (known CVEs become applicable)
   - File paths (local filesystem structure)

2. **Attack Surface**: Attackers can:
   - Identify specific frameworks/versions to exploit
   - Map internal system architecture
   - Discover admin endpoints or hidden functionality
   - Craft targeted exploitation attacks

3. **Compliance Violations**: OWASP A05:2021 highlights this. GDPR/PCI-DSS require secure error messages.

**Our Implementation**: Always returns generic 500 with logging, never exposing internals to clients.

#### 5.5 Request & Response Logging Filter

```java
@Provider
public class ApiLoggingFilter implements 
        ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger LOGGER = 
        Logger.getLogger(ApiLoggingFilter.class.getName());
    
    @Override
    public void filter(ContainerRequestContext requestContext) 
            throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info("Incoming Request: " + method + " " + uri);
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) 
            throws IOException {
        int status = responseContext.getStatus();
        LOGGER.info("Outgoing Response: HTTP " + status);
    }
}
```

**Why Use Filters for Cross-Cutting Concerns:**

1. **DRY Principle**: Logging in filters vs. manually in every method:
   - Filters: Write once, applies to all resources
   - Manual: Repeat in 50+ methods, maintenance nightmare

2. **Performance**: Filters are applied at container level, optimized by the framework

3. **Separation**: Business logic stays clean, logging concerns isolated

4. **Consistency**: All requests logged uniformly, no missed endpoints

5. **Flexibility**: Enable/disable logging globally without code changes

6. **Observability**: Centralized logging enables better monitoring and debugging

---

## Data Models

### Room
```java
public class Room {
    private String id;              // Unique identifier (e.g., "LIB-301")
    private String name;            // Human-readable name
    private int capacity;           // Maximum occupancy
    private List<String> sensorIds; // IDs of deployed sensors
}
```

### Sensor
```java
public class Sensor {
    private String id;              // Unique identifier (e.g., "TEMP-001")
    private String type;            // Category (Temperature, CO2, Occupancy, etc.)
    private String status;          // ACTIVE, MAINTENANCE, or OFFLINE
    private double currentValue;    // Most recent measurement
    private String roomId;          // Foreign key to Room
}
```

### SensorReading
```java
public class SensorReading {
    private String id;              // UUID of this reading event
    private long timestamp;         // Epoch milliseconds
    private double value;           // The recorded metric value
}
```

### ErrorResponse
```java
public class ErrorResponse {
    private int status;             // HTTP status code
    private String error;           // Exception class name
    private String message;         // Human-readable message
    private long timestamp;         // When error occurred
}
```

---

## Submission Checklist

- [x] Project hosted in public GitHub repository
- [x] README.md with complete project documentation
- [x] Step-by-step build and deployment instructions
- [x] Sample curl commands for all API endpoints
- [x] Comprehensive answers to all coursework questions
- [x] Video demonstration of Postman tests (submitted to Blackboard)
- [x] JAX-RS implementation (no Spring Boot)
- [x] In-memory data structures only (no database)
- [x] Custom exception mappers for error handling
- [x] Request/response logging filter
- [x] HATEOAS discovery endpoint

---

## Author

**Deveendra Rathnayake**  
University of Westminster  
5COSC022W - Client-Server Architectures  
April 2026
