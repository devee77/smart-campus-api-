# Smart Campus Sensor & Room Management API

A JAX-RS RESTful API built with Jersey deployed on Apache Tomcat, developed for the 5COSC022W Client-Server Architectures coursework.

---

## API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/` | Discovery — API metadata and resource links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors assigned) |
| GET | `/api/v1/sensors` | List all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history |
| POST | `/api/v1/sensors/{sensorId}/readings` | Record a new reading |

---

## How to Build and Run

### Prerequisites
- Java 11+
- Maven 3.6+
- Apache Tomcat 9.x

### Build
```bash
mvn clean package
```
This produces `target/smart-campus-api-1.0.0.war`.

### Deploy to Tomcat
1. Copy the WAR to Tomcat's webapps folder:
   ```bash
   cp target/smart-campus-api-1.0.0.war /path/to/tomcat/webapps/ROOT.war
   ```
2. Start Tomcat:
   ```bash
   /path/to/tomcat/bin/startup.sh   # Linux/Mac
   /path/to/tomcat/bin/startup.bat  # Windows
   ```
3. The API will be available at: `http://localhost:8080/api/v1/`

> **Tip:** If you deploy as `ROOT.war` the base URL is `http://localhost:8080/api/v1/`.
> If you deploy as `smart-campus-api.war`, the base URL becomes `http://localhost:8080/smart-campus-api/api/v1/`.

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. List all rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 3. Create a new room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-01","name":"Main Hall","capacity":200}'
```

### 4. Get a specific room
```bash
curl -X GET http://localhost:8080/api/v1/rooms/LIB-301
```

### 5. Attempt to delete a room that has sensors (expects 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 6. List all sensors filtered by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 7. Register a new sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"LIB-301"}'
```

### 8. Post a reading to an ACTIVE sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

### 9. Attempt to post a reading to a MAINTENANCE sensor (expects 403 Forbidden)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":15.0}'
```

### 10. Get reading history for a sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

---

## Conceptual Report

### Part 1.1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request** (per-request scope). This means each request gets its own fresh object, so instance variables are not shared between requests and there are no thread-safety concerns at the object level itself.

However, this per-request lifecycle creates an important implication for in-memory data storage: if you store data in instance fields of the resource class (e.g., a `HashMap` declared inside `RoomResource`), that data will be lost as soon as the request ends. Each new request creates a brand-new resource instance with an empty map.

To prevent data loss, the data must live **outside** the resource class, in a shared singleton — which is what the `DataStore` class implements. By using the Singleton pattern with `ConcurrentHashMap`, one shared instance holds all room, sensor, and reading data across all requests. `ConcurrentHashMap` is chosen over a plain `HashMap` because multiple threads can serve requests simultaneously; `ConcurrentHashMap` handles concurrent reads and writes without throwing `ConcurrentModificationException` or producing corrupted state.

---

### Part 1.2 — HATEOAS (Hypermedia as the Engine of Application State)

HATEOAS is the practice of embedding navigational links inside API responses so clients can discover what actions are available without relying on hardcoded URLs or external documentation.

For example, a response from `GET /api/v1` in this API includes links like:
```json
{ "rooms": { "href": "/api/v1/rooms", "methods": "GET, POST" } }
```

**Benefits over static documentation:**
- **Self-documenting:** Clients always know which endpoints exist and what methods they support just by reading the response.
- **Evolvable:** If an endpoint URL changes, the server updates the link in one place. Clients following links rather than hardcoding paths are not broken.
- **Reduced coupling:** The client does not need to know the URL structure upfront — it navigates from a root discovery response, just like a browser navigates from a homepage.

---

### Part 2.1 — Returning IDs vs Full Objects in List Responses

**Returning only IDs** (`["LIB-301", "LAB-102"]`):
- Very low bandwidth — ideal when the client only needs to know what exists.
- Forces extra round-trips: the client must call `GET /rooms/{id}` for each ID separately to get details.
- Poor developer experience; creates an N+1 request problem.

**Returning full room objects** (this implementation):
- One request returns everything the client needs.
- Higher bandwidth per response, but eliminates multiple follow-up calls.
- Superior for UI rendering, reporting, and any client that needs room details immediately.

For a campus management API where clients typically render a list of rooms with their names and capacities, returning full objects in the collection response is the correct trade-off.

---

### Part 2.2 — Is DELETE Idempotent?

**Yes, DELETE is idempotent** by HTTP specification — calling it multiple times should produce the same server state as calling it once.

In this implementation: the **first** `DELETE /rooms/HALL-01` removes the room and returns `204 No Content`. A **second** identical request finds no room, throws `ResourceNotFoundException`, and returns `404 Not Found`. The server state after both calls is identical (the room is gone), so idempotency is preserved at the state level. The response code differs between the first and subsequent calls, but this is acceptable — idempotency refers to side effects on the resource state, not to the HTTP status code returned.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

`@Consumes(MediaType.APPLICATION_JSON)` declares that the endpoint only accepts requests with `Content-Type: application/json`.

If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS will respond with **HTTP 415 Unsupported Media Type** automatically, before the method body is ever executed. The framework matches the incoming `Content-Type` header against the `@Consumes` annotation; if no match is found, the request is rejected at the framework level with no application code running.

---

### Part 3.2 — Query Parameter vs Path Segment for Filtering

**Path segment approach:** `GET /api/v1/sensors/type/CO2`
- Implies `CO2` is a discrete, identifiable resource — a fixed entity with its own identity.
- Semantically wrong for filtering: `type/CO2` suggests a specific "type resource", not a filtered view of the sensor collection.
- Makes optional filtering impossible without duplicating routes.

**Query parameter approach:** `GET /api/v1/sensors?type=CO2` (this implementation)
- Semantically correct: the base resource is `sensors`; the query parameter modifies the view.
- Optional by nature — `GET /api/v1/sensors` without the parameter returns all sensors.
- Supports multiple simultaneous filters: `?type=CO2&status=ACTIVE`.
- RESTful convention: path identifies the resource, query parameters refine/filter the collection.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The sub-resource locator pattern (`@Path("/{sensorId}/readings")` returning a new class instance) provides several architectural advantages:

1. **Separation of concerns:** `SensorController` handles sensor CRUD; `SensorReadingController` handles reading logic. Each class has one clear responsibility.
2. **Manageable class size:** A single "mega-controller" with all nested paths would quickly become hundreds of lines, difficult to read, test, and maintain.
3. **Reusable validation:** The locator method validates the parent sensor exists once, before delegating. The sub-resource class can then focus purely on reading logic.
4. **Independent testability:** `SensorReadingController` can be unit-tested in isolation without needing a full sensor context.
5. **Scalability:** As the API grows (e.g., adding `/sensors/{id}/alerts`, `/sensors/{id}/calibrations`), each concern gets its own dedicated class without bloating existing controllers.

---

### Part 5.2 — Why HTTP 422 is More Accurate than 404 for Missing References

`404 Not Found` means the **URL itself** points to a resource that does not exist — the thing identified by the path cannot be found.

`422 Unprocessable Entity` means the **request was well-formed** (valid JSON, correct Content-Type, valid URL) but the **business logic inside the payload is invalid** — the request cannot be processed because it references data that doesn't exist.

When a client posts a new sensor with `"roomId": "GHOST-99"`, the URL `/api/v1/sensors` is perfectly valid and was found. The problem is inside the JSON body — it references a room that doesn't exist. Using `404` here would mislead clients into thinking the `/sensors` endpoint doesn't exist. `422` correctly communicates: "your request reached us fine, but the content is logically invalid."

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to API consumers is a serious security risk:

1. **Framework and library disclosure:** Stack traces reveal exact versions of Jersey, Tomcat, Jackson, and Java — attackers can cross-reference known CVEs for those specific versions.
2. **Internal package structure:** Full class names and package paths (`com.smartcampus.repository.DataStore`) expose the application's architecture, making targeted attacks easier.
3. **File paths:** Stack traces often include absolute file system paths (e.g., `/home/ubuntu/app/...`), revealing server OS, directory structure, and deployment layout.
4. **Business logic leakage:** Method names in the trace reveal internal logic, helping attackers understand how to craft malicious inputs.
5. **Denial of service assistance:** Knowing which operations cause exceptions helps attackers trigger them deliberately at scale.

The `GlobalExceptionMapper` solves this by catching all `Throwable`, logging the full detail server-side only, and returning a generic `500` message to the client with no internal information.

---

### Part 5.5 — Why Use Filters for Cross-Cutting Concerns

Manually inserting `Logger.info()` calls inside every resource method has serious drawbacks:

1. **Code duplication:** Logging logic is repeated in every method — a violation of the DRY principle.
2. **Inconsistency risk:** Developers can forget to add logging in new methods, creating blind spots.
3. **Tight coupling:** Logging is mixed with business concerns; removing or changing it requires touching every method.
4. **Harder maintenance:** Changing log format (e.g., adding a request ID) requires modifying every single method.

A `ContainerRequestFilter` / `ContainerResponseFilter` applies logging **once**, automatically, to every endpoint in the application. New controllers and methods added in the future are logged immediately without any code change. This is the principle of cross-cutting concerns: behaviour that applies everywhere should be defined in one place.
