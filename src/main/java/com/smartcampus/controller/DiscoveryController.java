package com.smartcampus.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Part 1.2 — Discovery Endpoint.
 * GET /api/v1 returns API metadata with HATEOAS-style resource links.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryController {

    private static final Logger LOGGER = Logger.getLogger(DiscoveryController.class.getName());

    @GET
    public Response discover() {
        LOGGER.info("GET / — API discovery requested.");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor and Room Management API");
        response.put("version",     "1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("timestamp",   Instant.now().toString());

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name",       "Campus Facilities Team");
        contact.put("email",      "admin@smartcampus.ac.uk");
        contact.put("department", "Estates and Facilities Management");
        response.put("contact", contact);

        Map<String, Object> resources = new LinkedHashMap<>();
        resources.put("rooms",    buildLink("/api/v1/rooms",                       "GET, POST",   "List all rooms or create a new room"));
        resources.put("room",     buildLink("/api/v1/rooms/{roomId}",              "GET, DELETE", "Retrieve or decommission a specific room"));
        resources.put("sensors",  buildLink("/api/v1/sensors",                     "GET, POST",   "List sensors (supports ?type= filter) or register a new sensor"));
        resources.put("sensor",   buildLink("/api/v1/sensors/{sensorId}",          "GET",         "Retrieve a specific sensor by ID"));
        resources.put("readings", buildLink("/api/v1/sensors/{sensorId}/readings", "GET, POST",   "Get or append readings for a sensor"));
        response.put("resources", resources);

        return Response.ok(response).build();
    }

    private Map<String, Object> buildLink(String href, String methods, String description) {
        Map<String, Object> link = new LinkedHashMap<>();
        link.put("href",        href);
        link.put("methods",     methods);
        link.put("description", description);
        return link;
    }
}
